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
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author jlopezg8
 */
public class HipsterGraphsTest {
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
    public void testGetAdjacencyMatrix() {
        double[][] expResult = new double[][] {
            { 0,  0, -2,  0},
            { 4,  0,  3,  0},
            { 0,  0,  0,  2},
            { 0, -1,  0,  0}
        };
        double[][] result = HipsterGraphs.getAdjacencyMatrix(
                graph, verticesIndices);
        assertArrayEquals(expResult, result);
    }
}
