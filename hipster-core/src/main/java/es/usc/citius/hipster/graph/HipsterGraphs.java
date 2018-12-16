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

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains utility methods that operate on HipsterGraph objects.
 * @author jlopezg8
 */
public class HipsterGraphs {
    /**
     * Returns the adjacency matrix of the graph, which is a matrix A where
     * A[i][j] is the weight of the edge between the vertex with index i and the
     * vertex with index j, or 0 if there's no edge between the two vertices.
     * The indexing of the matrix will be determined by the order of the
     * iterator returned by {@link HipsterGraph#vertices}.
     * @param <V>   the type of the graph's vertices
     * @param graph the graph from which to get the adjacency matrix
     * @return the adjacency matrix of the graph
     */
    public static <V> double[][] getAdjacencyMatrix(HipsterGraph<V, Double> graph)
    {
        Map<V, Integer> verticesIndices = new HashMap<>();
        int nVertices = 0;
        for (V vertex : graph.vertices()) {
            verticesIndices.put(vertex, nVertices++);
        }
        return getAdjacencyMatrix(graph, verticesIndices);
    }
    
    /**
     * Returns the adjacency matrix of the graph, which is a matrix A where
     * A[i][j] is the weight of the edge between the vertex with index i and the
     * vertex with index j, or 0 if there's no edge between the two vertices.
     * verticesIndices is a map from vertex to its respective index on the
     * matrix, so it will determine the indexing of the matrix.
     * @param <V>   the type of the graph's vertices
     * @param graph the graph from which to get the adjacency matrix
     * @param verticesIndices   a map from vertex to its respective index on the
     *                          matrix
     * @return the adjacency matrix of the graph
     */
    public static <V> double[][] getAdjacencyMatrix(
            HipsterGraph<V, Double> graph, Map<V, Integer> verticesIndices)
    {
        int nVertices = verticesIndices.size();
        double[][] adjacencies = new double[nVertices][nVertices];
        for (GraphEdge<V, Double> edge : graph.edges()) {
            int vertex1Index = verticesIndices.get(edge.getVertex1());
            int vertex2Index = verticesIndices.get(edge.getVertex2());
            adjacencies[vertex1Index][vertex2Index] = edge.getEdgeValue();
            // for a A → B connection, the HashBasedHipster[Undirected]Graph
            // .edges() returns A → B twice, instead of A → B, B → A
            if (edge.getType() == GraphEdge.Type.UNDIRECTED) {
                adjacencies[vertex2Index][vertex1Index] = edge.getEdgeValue();
            }
        }
        return adjacencies;
    }
}
