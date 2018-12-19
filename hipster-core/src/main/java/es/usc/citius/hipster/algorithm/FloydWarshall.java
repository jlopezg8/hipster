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
import es.usc.citius.hipster.model.function.impl.BinaryOperation;
import es.usc.citius.hipster.util.Function;
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
 * This class does not subclass {@link Algorithm}, mainly because classes that
 * do aim to be graph-agnostic, and are framed around <b>progressively</b>
 * exploring the state space from a <b>single</b> initial state until reaching
 * any of several goal states; while the Floyd-Warshall algorithm is inherently
 * graph-centric, since it works with the graph's adjacency matrix (and thus the
 * <b>fully explored</b> state space), and does not expect a <b>single</b>
 * initial state or any goal states, since all states would be considered
 * initial and goal states.
 *
 * @author jlopezg8
 */
public class FloydWarshall {
    /**
     * Contains the outputs of a run of the Floyd-Warshall algorithm on a graph.
     * These consist of the distance matrix, which holds the costs of the
     * shortest paths between any two vertices of the graph, the successor
     * matrix, used to construct the respective paths, a map from vertex to
     * its respective index on the previous two matrices, and the {@link
     * es.usc.citius.hipster.model.function.impl.BinaryOperation} used to add
     * costs and represent the non-existence of paths.
     *
     * @param <V> the type of the graph's vertices
     * @param <E> the type of the graph's edge values
     */
    public static class Result<V, E extends Comparable<E>> {
        private final List<List<E>> distances;
        private final List<List<V>> successors;
        private final Function<V, Integer> vertexToIndex;
        private final BinaryOperation<E> op;

        private Result(List<List<E>> distances, List<List<V>> successors,
                Function<V, Integer> verticesIndices, BinaryOperation<E> op)
        {
            this.distances = distances;
            this.successors = successors;
            this.vertexToIndex = verticesIndices;
            this.op = op;
        }

        /**
         * Returns the distance matrix. This is a matrix (list of lists of edge
         * values) A, where {@code A.get(i).get(j)} is the cost of the shortest
         * path from the vertex with index i to the vertex with index j, or
         * {@code getOp().getMaxElem()} (defined to be the maximum cost) if
         * there's no path between the two vertices.
         *
         * @return  a matrix which holds the costs of the shortest paths between
         *          any two vertices
         * @see es.usc.citius.hipster.model.function.impl.BinaryOperation
         */
        public List<List<E>> getDistances() {
            return distances;
        }

        /**
         * Returns the successors matrix. This is a matrix (list of lists of
         * vertices) A, where {@code A.get(i).get(j)} is the successor of the
         * vertex with index i on the shortest path to the vertex with index j,
         * or {@code null} if there's no path between the two vertices. It's
         * used to construct the path between any two vertices by following the
         * chain of successors from the starting vertex to the ending vertex.
         *
         * @return a matrix used to construct the path between any two vertices
         */
        public List<List<V>> getSuccessors() {
            return successors;
        }

        /**
         * Returns a {@link es.usc.citius.hipster.util.Function} that maps a
         * vertex to its respective index on the distance or successor matrix,
         * or to -1 if the graph did not contain the vertex. For example, to get
         * the cost of the shortest path from vertex 'A' to vertex 'B': {@code
         * distances.get(vertexToIndex.apply('A')).get(vertexToIndex.apply('B'))
         * }
         *
         * @return a {@code Function} from vertex to index
         */
        public Function<V, Integer> getVertexToIndex() {
            return vertexToIndex;
        }

        /**
         * Returns the {@link
         * es.usc.citius.hipster.model.function.impl.BinaryOperation} used to
         * add costs and represent the non-existence of paths.
         *
         * @return the {@code BinaryOperation} used on the costs
         */
        public BinaryOperation<E> getOp() {
            return op;
        }

        private int indexOf(V vertex, String name) {
            int index = vertexToIndex.apply(vertex);
            if (index == -1) {
                throw new IllegalArgumentException(
                        "The graph did not contain the " + name + " vertex");
            }
            return index;
        }

        /**
         * Returns the cost of the shortest path between the two specified
         * vertices.
         * @param start the starting vertex of the path
         * @param end   the ending vertex of the path
         * @return  the cost of the shortest path between the two specified
         *          vertices
         */
        public E getDistance(V start, V end) {
            int startIndex = indexOf(start, "start");
            int endIndex = indexOf(end, "end");
            return distances.get(startIndex).get(endIndex);
        }

        /**
         * Returns {@code true} if there's a path between the two specified
         * vertices.
         * @param start the starting vertex of the path
         * @param end   the ending vertex of the path
         * @return  {@code true} if there's a path between the two specified
         *          vertices
         */
        public boolean existsPath(V start, V end) {
            int startIndex = indexOf(start, "start");
            int endIndex = indexOf(end, "end");
            return successors.get(startIndex).get(endIndex) != null;
        }

        /**
         * Returns the shortest path between the two specified vertices. The
         * path is represented by a sequence (a list) of vertices, including
         * both the start and end vertices. Note that:
         * <ul>
         * <li>If the two vertices are equal, a list with only this vertex is
         * returned.</li>
         * <li>If there's no path between the two vertices, an empty list is
         * returned.</li>
         * </ul>
         *
         * @param start the starting vertex of the path
         * @param end   the ending vertex of the path
         * @return  a list of vertices forming the shortest path between the two
         *          specified vertices
         */
        public List<V> getPath(V start, V end) {
            int startIndex = indexOf(start, "start");
            int endIndex = indexOf(end, "end");

            if (successors.get(startIndex).get(endIndex) == null) {
                return Collections.emptyList();
            }

            List<V> path = new ArrayList<>();
            path.add(start);
            while (!start.equals(end)) {
                start = successors.get(startIndex).get(endIndex);
                path.add(start);
                startIndex = vertexToIndex.apply(start);
            }
            return path;
        }
    }

    /**
     * Runs the Floyd-Warshall algorithm on the specified graph. The {@link
     * es.usc.citius.hipster.model.function.impl.BinaryOperation} is used to add
     * costs and represent the non-existence of paths, so as to generalize for
     * costs that are not necessarily of type Double.
     *
     * @param <V>   the type of the graph's vertices
     * @param <E>   the type of the graph's edge values
     * @param graph the graph on which to run the algorithm
     * @param op    the {@code BinaryOperation} to be used on the costs
     * @return an object which contains the outputs of the algorithm
     * @throws NegativeCycleException if one is detected
     * @see Result
     */
    public static <V, E extends Comparable<E>> Result<V, E> apply(
            HipsterGraph<V, E> graph, BinaryOperation<E> op)
    {
        // could really use a bidirectional map (index ↔ vertex), but would
        // need a dependency like Guava
        List<V> vertices = new ArrayList<>();
        final Map<V, Integer> verticesIndices = new HashMap<>();
        int nVertices = 0;
        for (V vertex : graph.vertices()) {
            vertices.add(vertex);
            verticesIndices.put(vertex, nVertices++);
        }
        Function<V, Integer> vertexToIndex = new Function<V, Integer>() {
            @Override
            public Integer apply(V vertex) {
                return verticesIndices.getOrDefault(vertex, -1);
            }
        };

        E zero = op.getIdentityElem(), inf = op.getMaxElem();
        List<List<E>> adjacencies = HipsterGraphs.getAdjacencyMatrix(
                graph, zero, vertexToIndex);
        List<List<E>> distances = new ArrayList<>(nVertices);
        List<List<V>> successors = new ArrayList<>(nVertices);
        for (int i = 0; i < nVertices; i++) {
            ArrayList<E> distancesRow = new ArrayList<>(nVertices);
            ArrayList<V> successorsRow = new ArrayList<>(nVertices);
            for (int j = 0; j < nVertices; j++) {
                E weight = adjacencies.get(i).get(j);
                boolean connected;
                if (i == j) {
                    if (weight.compareTo(zero) < 0) {
                        throw new NegativeCycleException();
                    } else { // zero <= weight
                        weight = zero;
                    }
                    connected = true;
                } else {
                    connected = weight.compareTo(zero) != 0;
                }
                distancesRow.add(connected? weight : inf);
                successorsRow.add(connected? vertices.get(j) : null);
            }
            distances.add(distancesRow);
            successors.add(successorsRow);
        }

        for (int i = 0; i < nVertices; i++) {
            for (int j = 0; j < nVertices; j++) {
                for (int k = 0; k < nVertices; k++) {
                    E d_ik = distances.get(i).get(k);
                    E d_ji = distances.get(j).get(i);
                    if (d_ik.compareTo(inf) < 0 && d_ji.compareTo(inf) < 0) {
                        E sum = op.apply(d_ik, d_ji);
                        if (sum.compareTo(distances.get(j).get(k)) < 0) {
                            distances.get(j).set(k, sum);
                            successors.get(j).set(k, successors.get(j).get(i));
                        }
                    }
                }
                if (distances.get(j).get(j).compareTo(zero) < 0) {
                    throw new NegativeCycleException();
                }
            }
        }

        return new Result<>(distances, successors, vertexToIndex, op);
    }

    /**
     * Runs the Floyd-Warshall algorithm on the specified graph. The graph's
     * edge values must be of type Double in order to use the default {@link
     * es.usc.citius.hipster.model.function.impl.BinaryOperation#doubleAdditionOp()}
     * on the costs.
     *
     * @param <V>   the type of the graph's vertices
     * @param graph the graph on which to run the algorithm
     * @return an object which contains the outputs of the algorithm
     * @throws NegativeCycleException if one is detected
     * @see Result
     */
    public static <V> Result<V, Double> apply(HipsterGraph<V, Double> graph) {
        return apply(graph, BinaryOperation.doubleAdditionOp());
    }
}
