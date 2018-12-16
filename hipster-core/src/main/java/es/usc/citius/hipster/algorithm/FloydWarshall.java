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
package es.usc.citius.hipster.algorithm;

import es.usc.citius.hipster.graph.HipsterGraph;
import es.usc.citius.hipster.graph.HipsterGraphs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Floyd-Warshall algorithm, which finds the shortest
 * paths between all pairs of vertices of a graph in a single run. See the
 * <a href="https://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm">
 * Wikipedia article</a> for more information.
 * <p>
 * This class does not subclass Algorithm, since Algorithm expects an
 * initialNode and a finalNode and is graph-agnostic, while FloydWarshall does
 * not expect an initialNode nor a finalNode and is graph-centric. So these are
 * fundamentally incompatible. TODO: maybe I should give it a try.
 * 
 * @author jlopezg8
 */
public class FloydWarshall {
    /**
     * 
     * Contains the outputs of a run of the Floyd-Warshall algorithm on a graph.
     * These consist of the distance matrix, which holds the costs of the
     * shortest paths between any two vertices of the graph, the successor
     * matrix, used to construct the respective paths, and a map from vertex to
     * its respective index on the previous two matrices.
     * @param <V> the type of the graph's vertices
     */
    public static class Result<V> {
        private final double[][] distances;
        private final List<List<V>> successors;
        private final Map<V, Integer> verticesIndices;
        
        private Result(double[][] distances, List<List<V>> successors,
                Map<V, Integer> verticesIndices)
        {
            this.distances = distances;
            this.successors = successors;
            this.verticesIndices = verticesIndices;
        }

        /**
         * Returns a matrix A, where A[i][j] is the cost of the shortest path
         * from the vertex with index i to the vertex with index j, or
         * Double.INFINITY if there's no path between the two vertices.
         * @return  a matrix which holds the costs of the shortest paths between
         *          any two vertices
         */
        public double[][] getDistances() {
            return distances;
        }

        /**
         * Returns a matrix (list of lists of vertices) A, where A.get(i).get(j)
         * is the successor of the vertex with index i on the shortest path to
         * the vertex with index j, or null if thre's no path between the two
         * vertices. It's used to construct the path between any two vertices by
         * following the chain of successors from the starting vertex to the
         * ending vertex.
         * @return a matrix used to construct the path between any two vertices
         */
        public List<List<V>> getSuccessors() {
            return successors;
        }

        /**
         * Returns a map from vertex to its respective index on the distance or
         * successor matrix. For example, to get the cost of the shortest path
         * from vertex 'A' to vertex 'B':
         * <code>
         *     distances[verticesIndices.get('A')][verticesIndices.get('B')]
         * </code>
         * @return  a map from vertex to its respective index on the distance or
         *          successor matrix
         */
        public Map<V, Integer> getVerticesIndices() {
            return verticesIndices;
        }
        
        /**
         * Returns the shortest path between the two specified vertices. The
         * path is represented by a sequence (a list) of vertices, including
         * both the start and end vertices.
         * If the two vertices are equal, a list with only this vertex is
         * returned.
         * If there's no path between the two vertices, an empty list is
         * returned.
         * @param start the starting vertex of the path
         * @param end   the ending vertex of the path
         * @return  a list of vertices forming the shortest path between the two
         *          specified vertices
         */
        public List<V> getPath(V start, V end) {
            if (!verticesIndices.containsKey(start)) {
                throw new IllegalArgumentException(
                        "The graph did not contain the start vertex");
            }
            if (verticesIndices.containsKey(end)) {
                throw new IllegalArgumentException(
                        "The graph did not contain the end vertex");
            }
            
            int startIndex = verticesIndices.get(start);
            int endIndex = verticesIndices.get(end);
            
            if (successors.get(startIndex).get(endIndex) == null) {
                return Collections.emptyList();
            }
            
            List<V> path = new ArrayList<>();
            path.add(start);
            while (!start.equals(end)) {
                start = successors.get(startIndex).get(endIndex);
                path.add(start);
                startIndex = verticesIndices.get(start);
            }
            return path;
        }
    }
    
    /**
     * Runs the Floyd-Warshall algorithm on the specified graph. The graph's
     * edge values must be of type Double, for the moment.
     * @param <V>   the type of the graph's vertices
     * @param graph the graph on which to run the algorithm
     * @return an object which contains the outputs of the algorithm
     * @see Result
     */
    public static <V> Result<V> apply(HipsterGraph<V, Double> graph) {
        // could really use a bidirectional map (index ↔ vertex), but would
        // need a dependency like Guava
        List<V> vertices = new ArrayList<>();
        Map<V, Integer> verticesIndices = new HashMap<>();
        int nVertices = 0;
        for (V vertex : graph.vertices()) {
            vertices.add(vertex);
            verticesIndices.put(vertex, nVertices++);
        }
        
        double[][] adjacencies = HipsterGraphs.getAdjacencyMatrix(
                graph, verticesIndices);
        double[][] distances = new double[nVertices][nVertices];
        List<List<V>> successors = new ArrayList<>(nVertices);
        for (int i = 0; i < nVertices; i++) {
            ArrayList<V> successorsRow = new ArrayList<>(nVertices);
            for (int j = 0; j < nVertices; j++) {
                double weight = adjacencies[i][j];
                boolean connected = i == j || weight != 0;
                distances[i][j] = connected? weight : Double.POSITIVE_INFINITY;
                successorsRow.add(connected? vertices.get(j) : null);
            }
            successors.add(successorsRow);
        }
        
        for (int i = 0; i < nVertices; i++) {
            for (int j = 0; j < nVertices; j++) {
                for (int k = 0; k < nVertices; k++) {
                    double sum = distances[i][k] + distances[j][i];
                    if (sum < distances[j][k]) {
                        distances[j][k] = sum;
                        successors.get(j).set(k, successors.get(j).get(i));
                    }
                }
            }
        }
        
        return new Result<>(distances, successors, verticesIndices);
    }
}
