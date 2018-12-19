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
package es.usc.citius.lab.hipster.algorithm;

import es.usc.citius.hipster.algorithm.FloydWarshall;
import es.usc.citius.hipster.algorithm.NegativeCycleException;
import es.usc.citius.hipster.graph.GraphBuilder;
import es.usc.citius.hipster.graph.HipsterGraph;
import es.usc.citius.hipster.model.function.BinaryFunction;
import es.usc.citius.hipster.model.function.impl.BinaryOperation;
import es.usc.citius.hipster.util.Function;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author jlopezg8
 */
public class FloydWarshallTest {
    // 'A': 0, 'B': 1, ..., 'Z': 26
    static Function<Character, Integer> vertexToIndex = new Function<Character, Integer>() {
        @Override
        public Integer apply(Character vertex) {
            char letter = vertex;
            return ('A' <= letter && letter <= 'Z')? letter - 'A' : -1;
        }
    };

    @Test
    public void testApply1() {
        HipsterGraph<Character, Double> graph = GraphBuilder.<Character, Double>create()
                .connect('A').to('C').withEdge(-2d)
                .connect('B').to('A').withEdge(4d)
                .connect('B').to('C').withEdge(3d)
                .connect('C').to('D').withEdge(2d)
                .connect('D').to('B').withEdge(-1d)
                .createDirectedGraph();
        double[][] distances = new double[][] {
            //A   B   C   D
            { 0, -1, -2,  0 }, // A
            { 4,  0,  2,  4 }, // B
            { 5,  1,  0,  2 }, // C
            { 3, -1,  1,  0 }  // D
        };
        char[][] successors = new char[][] {
            // A    B    C    D
            { 'A', 'C', 'C', 'C' }, // A
            { 'A', 'B', 'A', 'A' }, // B
            { 'D', 'D', 'C', 'D' }, // C
            { 'B', 'B', 'B', 'D' }  // D
        };
        FloydWarshall.Result<Character, Double> result = FloydWarshall.apply(graph);
        assertEquals(matrixToList(distances), result.getDistances());
        assertEquals(matrixToList(successors), result.getSuccessors());
        assertEquals(true, result.existsPath('B', 'B'));
        assertEquals(-1, result.getDistance('D', 'B').intValue());
        assertEquals(Arrays.asList('C', 'D', 'B', 'A'), result.getPath('C', 'A'));
    }

    @Test
    public void testApply2() {
        HipsterGraph<Character, Integer> graph = GraphBuilder.<Character, Integer>create()
                .connect('A').to('B').withEdge(8)
                .connect('A').to('D').withEdge(1)
                .connect('A').to('E').withEdge(1)
                .connect('B').to('C').withEdge(1)
                .connect('C').to('A').withEdge(4)
                .connect('C').to('E').withEdge(-1)
                .connect('D').to('B').withEdge(2)
                .connect('D').to('C').withEdge(9)
                .createDirectedGraph();
        int M = Integer.MAX_VALUE;
        int[][] distances = new int[][] {
            //A   B   C   D   E
            { 0,  3,  4,  1,  1 }, // A
            { 5,  0,  1,  6,  0 }, // B
            { 4,  7,  0,  5, -1 }, // C
            { 7,  2,  3,  0,  2 }, // D
            { M,  M,  M,  M,  0 }  // E
        };
        char[][] successors = new char[][] {
            // A    B    C    D    E
            { 'A', 'D', 'D', 'D', 'E' }, // A
            { 'C', 'B', 'C', 'C', 'C' }, // B
            { 'A', 'A', 'C', 'A', 'E' }, // C
            { 'B', 'B', 'B', 'D', 'B' }, // D
            {  0 ,  0 ,  0 ,  0 , 'E' }  // E
        };
        BinaryOperation<Integer> op = new BinaryOperation<Integer>(
                new BinaryFunction<Integer>() {
                    @Override
                    public Integer apply(Integer a, Integer b) {
                        return a + b;
                    }
                }, 0, Integer.MAX_VALUE);
        FloydWarshall.Result<Character, Integer> result = FloydWarshall.apply(
                graph, op);
        assertEquals(matrixToList(distances), result.getDistances());
        assertEquals(matrixToList(successors), result.getSuccessors());
        assertEquals(false, result.existsPath('E', 'C'));
        assertEquals(5, result.getDistance('B', 'A').intValue());
        assertEquals(Arrays.asList('C', 'A', 'D', 'B'), result.getPath('C', 'B'));
        assertEquals(Collections.singletonList('E'), result.getPath('E', 'E'));
        assertEquals(Collections.emptyList(), result.getPath('E', 'B'));
    }

    @Test(expected = NegativeCycleException.class)
    public void testApplyWithNegativeCycle1() {
        HipsterGraph<Character, Double> graph = GraphBuilder.<Character, Double>create()
                .connect('A').to('A').withEdge(-1d)
                .connect('A').to('B').withEdge(0d)
                .connect('B').to('A').withEdge(1d)
                .createDirectedGraph();
        FloydWarshall.apply(graph);
    }
    
    @Test(expected = NegativeCycleException.class)
    public void testApplyWithNegativeCycle2() {
        HipsterGraph<Character, Double> graph = GraphBuilder.<Character, Double>create()
                .connect('A').to('B').withEdge(5d)
                .connect('A').to('C').withEdge(4d)
                .connect('B').to('D').withEdge(3d)
                .connect('D').to('C').withEdge(2d)
                .connect('C').to('B').withEdge(-6d)
                .createDirectedGraph();
        FloydWarshall.apply(graph);
    }

    static List<List<Character>> matrixToList(char[][] matrix) {
        List<List<Character>> list = new ArrayList<List<Character>>(matrix.length);
        for (char[] matrixRow : matrix) {
            List<Character> listRow = new ArrayList<Character>(matrixRow.length);
            for (char e : matrixRow) {
                // 0 → null only so null successors can be tested
                listRow.add(e != 0? e : null);
            }
            list.add(listRow);
        }
        return list;
    }

    static List<List<Double>> matrixToList(double[][] matrix) {
        List<List<Double>> list = new ArrayList<List<Double>>(matrix.length);
        for (double[] matrixRow : matrix) {
            List<Double> listRow = new ArrayList<Double>(matrixRow.length);
            for (double e : matrixRow) {
                listRow.add(e);
            }
            list.add(listRow);
        }
        return list;
    }

    static List<List<Integer>> matrixToList(int[][] matrix) {
        List<List<Integer>> list = new ArrayList<List<Integer>>(matrix.length);
        for (int[] matrixRow : matrix) {
            List<Integer> listRow = new ArrayList<Integer>(matrixRow.length);
            for (int e : matrixRow) {
                listRow.add(e);
            }
            list.add(listRow);
        }
        return list;
    }
}
