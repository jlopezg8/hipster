/*
 * Copyright 2018 Centro de Investigación en Tecnoloxías da Información (CITIUS),
 * University of Santiago de Compostela.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.usc.citius.hipster.graph;

import es.usc.citius.hipster.util.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains utility methods that operate on HipsterGraph objects.
 *
 * @author jlopezg8
 */
public class HipsterGraphs {
    /**
     * Returns the adjacency matrix of the graph. This is a matrix (list of
     * lists of edge values) A, where {@code A.get(i).get(j)} is the weight of
     * the edge between the vertex with index i and the vertex with index j, or
     * {@code zero} if there's no edge between the two vertices. The indexing of
     * the matrix will be determined by the order of the iterator returned by
     * {@link HipsterGraph#vertices()}.
     *
     * @param <V>   the type of the graph's vertices
     * @param <E>   the type of the graph's edge values
     * @param graph the graph from which to get the adjacency matrix
     * @param zero  an edge value that indicates the non-existence of an edge
     *              between two vertices. It should not be {@code null} so it
     *              can be unboxed (if {@code E} is a primitive wrapper class)
     *              and/or {@link Comparable}.
     * @return the adjacency matrix of the graph
     */
    public static <V, E> List<List<E>> getAdjacencyMatrix(
            HipsterGraph<V, E> graph, E zero)
    {
        final Map<V, Integer> verticesIndices = new HashMap<>();
        int nVertices = 0;
        for (V vertex : graph.vertices()) {
            verticesIndices.put(vertex, nVertices++);
        }
        Function<V, Integer> vertexToIndex = new Function<V, Integer>() {
            @Override
            public Integer apply(V vertex) {
                return verticesIndices.getOrDefault(vertex, -1);
            }
        };
        return getAdjacencyMatrix(graph, zero, vertexToIndex);
    }

    /**
     * Returns the adjacency matrix of the graph. This is a matrix (list of
     * lists of edge values) A, where {@code A.get(i).get(j)} is the weight of
     * the edge between the vertex with index i and the vertex with index j, or
     * {@code zero} if there's no edge between the two vertices. {@code
     * verticesIndices} is a {@link es.usc.citius.hipster.util.Function} that
     * maps a vertex to its respective index on the matrix, so it will determine
     * the indexing of the matrix.
     *
     * @param <V>   the type of the graph's vertices
     * @param <E>   the type of the graph's edge values
     * @param graph the graph from which to get the adjacency matrix
     * @param zero  an edge value that indicates the non-existence of an edge
     *              between two vertices. It should not be {@code null} so it
     *              can be unboxed (if {@code E} is a primitive wrapper class)
     *              and/or {@link Comparable}.
     * @param vertexToIndex a {@code Function} from vertex to index
     * @return the adjacency matrix of the graph
     */
    public static <V, E> List<List<E>> getAdjacencyMatrix(
            HipsterGraph<V, E> graph, E zero, Function<V, Integer> vertexToIndex)
    {
        int nVertices = getNumVertices(graph);
        List<List<E>> adjacencies = new ArrayList<>(nVertices);
        for (int i = 0; i < nVertices; i++) {
            ArrayList<E> adjacenciesRow = new ArrayList<>(nVertices);
            for (int j = 0; j < nVertices; j++) {
                adjacenciesRow.add(zero);
            }
            adjacencies.add(adjacenciesRow);
        }

        for (GraphEdge<V, E> edge : graph.edges()) {
            int vertex1Index = vertexToIndex.apply(edge.getVertex1());
            int vertex2Index = vertexToIndex.apply(edge.getVertex2());
            adjacencies.get(vertex1Index).set(vertex2Index,
                    edge.getEdgeValue());
            // for a A → B connection, the HashBasedHipster[Undirected]Graph
            // .edges() returns A → B twice, instead of A → B, B → A
            if (edge.getType() == GraphEdge.Type.UNDIRECTED) {
                adjacencies.get(vertex2Index).set(vertex1Index,
                        edge.getEdgeValue());
            }
        }

        return adjacencies;
    }

    // should really be part of the HipsterGraph interface
    /**
     * Returns the number of vertices of the specified graph.
     * @param graph the graph from which to get the number of vertices
     * @return the number of vertices of the specified graph
     */
    public static int getNumVertices(HipsterGraph graph) {
        int nVertices = 0;
        for (Object vertex : graph.vertices()) nVertices++;
        return nVertices;
    }
}
