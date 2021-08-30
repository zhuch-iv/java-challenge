package com.zerohub.challenge.graph;

import com.zerohub.challenge.application.RateRepository;
import com.zerohub.challenge.currency.Rate;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class RateGraphRepository implements RateRepository {

    private Graph<String, Rate> graph;

    public RateGraphRepository() {
        this.graph = createGraph();
    }

    @Override
    public boolean containsCurrency(String currency) {
        return graph.containsVertex(currency);
    }

    @Override
    @Cacheable(value = "rates", key = "#from + #to")
    public Optional<Rate> getRate(String from, String to) {
        return Optional.ofNullable(
            new DijkstraShortestPath<>(graph).getPath(from, to)
        )
            .map(
                path -> path.getEdgeList()
                    .stream()
                    .reduce(Rate::exchange)
                    .orElseThrow()
            );
    }

    @Override
    @CacheEvict(value = "rates", allEntries = true)
    public void putRate(Rate rate) {
        graph.addVertex(rate.getBase());
        graph.addVertex(rate.getQuote());

        graph.addEdge(rate.getBase(), rate.getQuote(), rate);
        graph.setEdgeWeight(rate, rate.getValue().doubleValue());

        Rate reverse = rate.reverse();
        graph.addEdge(rate.getQuote(), rate.getBase(), reverse);
        graph.setEdgeWeight(reverse, reverse.getValue().doubleValue());
    }

    @CacheEvict(value = "rates", allEntries = true)
    public void removeAll() {
        graph = createGraph();
    }

    private Graph<String, Rate> createGraph() {
        return new AsSynchronizedGraph<>(
            GraphTypeBuilder
                .directed()
                .weighted(true)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .vertexClass(String.class)
                .edgeClass(Rate.class)
                .buildGraph()
        );
    }
}
