package com.example.openaiImage.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.example.openaiImage.model.Intent;
import com.example.openaiImage.model.IntentDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.StringReader;
import java.util.*;

@Service
public class HybridSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingService embeddingService;
    private final IndexingProgressService progressService;

    @Value("${elasticsearch.index.intent}")
    private String indexName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HybridSearchService(ElasticsearchClient elasticsearchClient, EmbeddingService embeddingService, IndexingProgressService progressService) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingService = embeddingService;
        this.progressService = progressService;
    }

    public void initializeIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                createIndex();
                Thread.sleep(2000);
            }

            long currentCount = getDocumentCount();
            if (currentCount == 0) {
                indexSampleData();
                System.out.println("Sample data indexed successfully");
            } else {
                System.out.println("Index already has " + currentCount + " documents, skipping sample data indexing");
            }
        } catch (Exception e) {
            System.err.println("Error initializing index: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public long getDocumentCount() throws Exception {
        try {
            return elasticsearchClient.count(c -> c.index(indexName)).count();
        } catch (Exception e) {
            return 0;
        }
    }

    public void deleteIndex() throws Exception {
        boolean exists = elasticsearchClient.indices()
                .exists(ExistsRequest.of(e -> e.index(indexName)))
                .value();

        if (exists) {
            elasticsearchClient.indices().delete(d -> d.index(indexName));
            System.out.println("Index deleted: " + indexName);
        }
    }

    public void reindexData() throws Exception {
        boolean exists = elasticsearchClient.indices()
                .exists(ExistsRequest.of(e -> e.index(indexName)))
                .value();

        if (!exists) {
            createIndex();
            Thread.sleep(3000);
        }

        indexSampleData();
        System.out.println("Reindexing completed");
    }

    private void createIndex() throws Exception {
        String mappings = """
                {
                  "settings": {
                    "number_of_shards": 1,
                    "number_of_replicas": 0
                  },
                  "mappings": {
                    "properties": {
                      "intent": { "type": "keyword" },
                      "query": { "type": "text", "analyzer": "standard" },
                      "keywords": { "type": "keyword" },
                      "embedding": {
                        "type": "dense_vector",
                        "dims": 1536,
                        "index": true,
                        "similarity": "cosine"
                      }
                    }
                  }
                }
                """;

        elasticsearchClient.indices().create(
                CreateIndexRequest.of(c -> c
                        .index(indexName)
                        .withJson(new StringReader(mappings))
                )
        );

        System.out.println("Index created: " + indexName);
    }

    private void indexSampleData() throws Exception {
        Map<Intent, List<String>> sampleQueries = Map.of(
                Intent.CARD_RECOMMENDATION, List.of("카드 추천해줘", "어떤 카드가 좋을까", "나한테 맞는 카드 찾아줘"),
                Intent.CARD_GUIDE, List.of("카드 혜택 알려줘", "카드 정보", "이 카드 연회비 얼마야"),
                Intent.EVENT_INFO, List.of("이벤트 있어?", "프로모션", "진행중인 행사 알려줘"),
                Intent.FINANCIAL_LOAN, List.of("대출 받고 싶어", "대출 상담", "금리 얼마야"),
                Intent.AUTO_TRANSFER, List.of("자동이체 등록", "자동이체 해지", "정기결제 설정"),
                Intent.TRANSACTION_HISTORY, List.of("거래내역 보여줘", "지난달 사용내역", "어제 결제한거 조회", "이번달 카드 사용 내역"),
                Intent.ACTUAL_EXPENSE, List.of("실비 청구", "보험 청구", "의료비 환급"),
                Intent.HOT_DEAL, List.of("핫딜", "특가 상품", "할인 상품 있어")
        );

        int totalDocs = sampleQueries.values().stream().mapToInt(List::size).sum();
        progressService.startIndexing(totalDocs);

        int id = 1;
        int currentDoc = 0;
        for (Map.Entry<Intent, List<String>> entry : sampleQueries.entrySet()) {
            Intent intent = entry.getKey();
            for (String query : entry.getValue()) {
                currentDoc++;
                progressService.updateProgress(currentDoc, "색인 중: " + query + " (" + currentDoc + "/" + totalDocs + ")");
                System.out.println("Indexing " + currentDoc + "/" + totalDocs + ": " + query);

                List<String> keywords = embeddingService.extractKeywords(query);
                List<Double> embedding = embeddingService.generateEmbedding(query);

                if (embedding.isEmpty()) {
                    System.err.println("Failed to generate embedding for: " + query);
                    continue;
                }

                IntentDocument doc = new IntentDocument(
                        String.valueOf(id++),
                        intent,
                        query,
                        keywords,
                        embedding
                );

                indexDocument(doc);
                Thread.sleep(500);
            }
        }

        progressService.finishIndexing();
        System.out.println("Sample data indexed successfully - total: " + (id - 1) + " documents");
    }

    public void indexDocument(IntentDocument document) throws Exception {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("intent", document.getIntent().name());
        docMap.put("query", document.getQuery());
        docMap.put("keywords", document.getKeywords());
        docMap.put("embedding", document.embedding());

        elasticsearchClient.index(IndexRequest.of(i -> i
                .index(indexName)
                .id(document.getId())
                .document(docMap)
        ));
    }

    public Intent searchIntent(String userQuery) {
        try {
            List<String> keywords = embeddingService.extractKeywords(userQuery);
            List<Double> queryEmbedding = embeddingService.generateEmbedding(userQuery);

            if (queryEmbedding.isEmpty()) {
                return Intent.UNKNOWN;
            }

            Query keywordQuery = QueryBuilders.terms()
                    .field("keywords")
                    .terms(t -> t.value(keywords.stream()
                            .map(k -> co.elastic.clients.elasticsearch._types.FieldValue.of(k))
                            .toList()))
                    .build()._toQuery();

            Map<String, co.elastic.clients.json.JsonData> scriptParams = new HashMap<>();
            scriptParams.put("query_vector", co.elastic.clients.json.JsonData.of(queryEmbedding));

            Query embeddingQuery = QueryBuilders.scriptScore()
                    .query(QueryBuilders.matchAll().build()._toQuery())
                    .script(s -> s
                            .source("cosineSimilarity(params.query_vector, 'embedding') + 1.0")
                            .params(scriptParams)
                    )
                    .build()._toQuery();

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(q -> q.bool(b -> b
                            .should(keywordQuery)
                            .should(embeddingQuery)
                    ))
                    .size(1)
            );

            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

            if (!response.hits().hits().isEmpty()) {
                Hit<Map> hit = response.hits().hits().get(0);
                Map<String, Object> source = hit.source();
                if (source != null && source.containsKey("intent")) {
                    String intentName = (String) source.get("intent");
                    return Intent.valueOf(intentName);
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching intent: " + e.getMessage());
            e.printStackTrace();
        }

        return Intent.UNKNOWN;
    }
}
