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
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author jlopezg8
 */
public class HipsterGraphsTest {
    // 'A': 0, 'B': 1, ..., 'Z': 26
    static Function<Character, Integer> vertexToIndex = new Function<Character, Integer>() {
        @Override
        public Integer apply(Character vertex) {
            char letter = vertex;
            return ('A' <= letter && letter <= 'Z')? letter - 'A' : -1;
        }
    };

    @Test
    public void testGetAdjacencyMatrix1() {
        HipsterGraph<Character, Double> graph = GraphBuilder.<Character, Double>create()
                .connect('A').to('B').withEdge(2d)
                .connect('A').to('C').withEdge(-1d)
                .connect('B').to('C').withEdge(-2d)
                .createUndirectedGraph();
        double[][] adjacencies = new double[][] {
            // A   B   C
            {  0,  2, -1 }, // A
            {  2,  0, -2 }, // B
            { -1, -2,  0 }  // C
        };
        List<List<Double>> expResult = matrixToList(adjacencies);
        List<List<Double>> result = HipsterGraphs.getAdjacencyMatrix(
                graph, 0d, vertexToIndex);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetAdjacencyMatrix2() {
        HipsterGraph<Character, Double> graph = GraphBuilder.<Character, Double>create()
                .connect('A').to('A').withEdge(-2d)
                .connect('A').to('B').withEdge(-1d)
                .connect('B').to('A').withEdge(3d)
                .createDirectedGraph();
        double[][] adjacencies = new double[][] {
            // A   B
            { -2, -1 }, // A
            {  3,  0 }  // B
        };
        List<List<Double>> expResult = matrixToList(adjacencies);
        List<List<Double>> result = HipsterGraphs.getAdjacencyMatrix(
                graph, 0d, vertexToIndex);
        assertEquals(expResult, result);
    }

    static List<List<Double>> matrixToList(double[][] matrix) {
        List<List<Double>> list = new ArrayList<>(matrix.length);
        for (double[] matrixRow : matrix) {
            List<Double> listRow = new ArrayList<>(matrixRow.length);
            for (double e : matrixRow) {
                listRow.add(e);
            }
            list.add(listRow);
        }
        return list;
    }
}
