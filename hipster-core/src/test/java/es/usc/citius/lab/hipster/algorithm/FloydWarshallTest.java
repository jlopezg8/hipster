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
import es.usc.citius.hipster.graph.GraphBuilder;
import es.usc.citius.hipster.graph.HipsterGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author jlopezg8
 */
public class FloydWarshallTest {
    static HipsterGraph<Character, Double> graph;
    static Map<Character, Integer> verticesIndices;
    
    @BeforeClass
    public static void setUpClass() {
        graph = GraphBuilder.<Character, Double>create()
                .connect('A').to('C').withEdge(-2d)
                .connect('B').to('A').withEdge(4d)
                .connect('B').to('C').withEdge(3d)
                .connect('C').to('D').withEdge(2d)
                .connect('D').to('B').withEdge(-1d)
                .createDirectedGraph();
        
        verticesIndices = new HashMap<>();
        for (char c = 'A', i = 0; c <= 'D'; c++, i++) {
            verticesIndices.put(c, (int) i);
        }
    }
    
    @Test
    public void testApply() {
        double[][] expDistances = new double[][] {
            { 0, -1, -2,  0},
            { 4,  0,  2,  4},
            { 5,  1,  0,  2},
            { 3, -1,  1,  0}
        };
        
        char[][] successors = new char[][] {
            {'A', 'C', 'C', 'C'},
            {'A', 'B', 'A', 'A'},
            {'D', 'D', 'C', 'D'},
            {'B', 'B', 'B', 'D'}
        };
        int nVertices = successors.length;
        List<List<Character>> expSuccessors = new ArrayList<>(nVertices);
        for (char[] successorsRow : successors) {
            List<Character> expSuccessorsRow = new ArrayList<>(nVertices);
            for (char successor : successorsRow) {
                expSuccessorsRow.add(successor);
            }
            expSuccessors.add(expSuccessorsRow);
        }
        
        FloydWarshall.Result<Character> result = FloydWarshall.apply(graph);
        assertArrayEquals(expDistances, result.getDistances());
        assertEquals(expSuccessors, result.getSuccessors());
        assertEquals(verticesIndices, result.getVerticesIndices());
    }
}
