package me.justin.culminating;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainUtils {

    //Pairs of vertices will be extruded downwards if they are in order of left to right
    public static TerrainSection[] generatePath(World world, Vector2[] points, float thickness, float x, float y, float mass) {
        TerrainSection[] path = new TerrainSection[points.length-1];

        for (int i=0; i<points.length-1; i++) {
            Vector2 topLeft = points[i];
            Vector2 topRight = points[i+1];

            Vector2 topEdge = topRight.cpy().sub(topLeft);
            Vector2 normal = new Vector2(-topEdge.y, topEdge.x).nor();

            Vector2 bottomRight = topRight.cpy().sub(normal.cpy().scl(thickness));
            Vector2 bottomLeft = topLeft.cpy().sub(normal.cpy().scl(thickness));

            path[i] = new TerrainSectionPolygon(world, x, y, new Vector2[] {bottomLeft, bottomRight, topRight, topLeft}, mass);
        }

        return path;
    }

    //Digraph representing the boundary pixels
    private static class CollisionImageGraph {

        public static class Edge {
            public final Vector2 a, b;
            public final int weight;

            public Edge(Vector2 a, Vector2 b, int weight) {this.a = a; this.b = b; this.weight = weight;}

            public Vector2 other(Vector2 v) {
                if (a.equals(v)) return b;
                else if (b.equals(v)) return a;
                else return null;
            }
        }

        public HashMap<Vector2, LinkedList<Edge>> adjacencyList = new HashMap<Vector2, LinkedList<Edge>>();

        //Use digraph so when it is built, there are no dupes
        // (as every boundary point is visited, and thus makes a directed edge from it to its boundary neighbours already)
        public void add(Vector2 a, Vector2 b, int weight) {
            if (!adjacencyList.containsKey(a)) adjacencyList.put(a, new LinkedList<Edge>());
//            if (!adjacencyList.containsKey(b)) adjacencyList.put(b, new LinkedList<Edge>());

            adjacencyList.get(a).add(new Edge(a,b, weight));
//            adjacencyList.get(b).add(new Edge(b,a, weight));
        }

        public Iterable<Edge> adj(Vector2 v) {
            if (adjacencyList.containsKey(v)) return adjacencyList.get(v);
            else return new ArrayList<Edge>();
        }

        public Iterable<Vector2> vertices() {
            return adjacencyList.keySet();
        }
    }

    private static CollisionImageGraph buildGraph(BufferedImage img, float scale) {
        CollisionImageGraph graph = new CollisionImageGraph();

        for (int x=0; x<img.getWidth(); x++) {
            for (int y=0; y<img.getHeight(); y++) {

                if (isBoundary(x,y,img)) {
                    Vector2 curr = new Vector2((x + 0.5f)*scale, getWorldY(y - 0.5f, img)*scale);

                    for (int nx = MathUtils.clamp(x-1, 0, img.getWidth()-1); nx<= MathUtils.clamp(x+1, 0, img.getWidth()-1); nx++) {
                        for (int ny = MathUtils.clamp(y-1, 0, img.getHeight()-1); ny<= MathUtils.clamp(y+1, 0, img.getHeight()-1); ny++) {

                            if (nx == x && ny == y) continue;

                            if (isBoundary(nx, ny, img)) {
                                graph.add(curr, new Vector2((nx + 0.5f)*scale, getWorldY(ny - 0.5f, img)*scale), 1);
                            }
                        }
                    }
                }
            }
        }

        return graph;
    }

    private static boolean isBoundary(int x, int y, BufferedImage img) {

        if (img.getRGB(x,y) != 0xFF000000) return false; //Not solid

        for (int nx = MathUtils.clamp(x-1, 0, img.getWidth()-1); nx<= MathUtils.clamp(x+1, 0, img.getWidth()-1); nx++) {
            for (int ny = MathUtils.clamp(y-1, 0, img.getHeight()-1); ny<= MathUtils.clamp(y+1, 0, img.getHeight()-1); ny++) {
                if (nx == x+1 && ny == y+1 || nx == x-1 && ny == y+1
                        || nx == x-1 && ny == y-1 || nx == x+1 && ny == y-1) continue;
                if (nx == x && ny == y) continue;

                if (img.getRGB(nx,ny) != 0xFF000000) return true; //white
            }
        }

        return false;
    }

    //Java's image libraries have origin in top left, libgdx in bottom left
    private static float getWorldY(float y, BufferedImage img) {
        return img.getHeight()-y-1;
    }

    private static void discardVertex(CollisionImageGraph g, Vector2 v) {
        //Connect each neighbour to each other neighbour to make this vertex redundant
        for (CollisionImageGraph.Edge e : g.adj(v)) {
            Vector2 n = e.other(v);

            for (CollisionImageGraph.Edge e2 : g.adj(v)) {
                Vector2 n2 = e2.other(v);

                if (!n.equals(n2)) g.add(n, n2, 1);
            }
        }

        //Remove all links to this vertex
        // (under the assumption that there are no unpaired directed edges
        for (CollisionImageGraph.Edge e : g.adj(v)) {
            Vector2 vertex = e.other(v);

            Iterator<CollisionImageGraph.Edge> itr = g.adj(vertex).iterator();

            while (itr.hasNext()) {
                CollisionImageGraph.Edge next = itr.next();
                if (next.other(vertex).equals(v)) itr.remove();
            }
        }

        g.adjacencyList.remove(v);
    }


    private static float curvature (Vector2 v1, Vector2 v2, Vector2 v3) {
        Vector2 midPoint = new Vector2((v1.x + v3.x)/2f, (v1.y + v3.y)/2f);
        return midPoint.dst2(v2);
    }

    private static void simplifyGraph(CollisionImageGraph g, float smoothness) {

        System.out.println("Graph size before simplification: " + g.adjacencyList.size());

        Stack<Vector2> toVisit = new Stack<Vector2>();
        Set<Vector2> visited = new HashSet<Vector2>();

        while (true) {

            Vector2 next = null;

            for (Vector2 v : g.vertices()) {
                if (!visited.contains(v)) {
                    next = v;
                    break;
                }
            }

            if (next == null) break;
            toVisit.add(next);

            while (!toVisit.isEmpty()) {
                Vector2 v = toVisit.pop();
                visited.add(v);

                //Find all neighbour -> v -> other neighbour sections
                //If we find none that are sharp, we can remove this edge

                boolean sharp = false;

                for (CollisionImageGraph.Edge e1 : g.adj(v)) {
                    Vector2 n1 = e1.other(v);

                    for (CollisionImageGraph.Edge e2 : g.adj(v)) {
                        Vector2 n2 = e2.other(v);
                        if (n1.equals(n2)) continue;

                        if (curvature(n1, v, n2) > smoothness) {
                            sharp = true;
                            break;
                        }
                    }

                    if (sharp) break;
                }

                if (!sharp) discardVertex(g,v);

                else for (CollisionImageGraph.Edge e : g.adj(v)) {
                    if (!visited.contains(e.other(v)) && !toVisit.contains(e.other(v))) toVisit.add(e.other(v));
                }
            }
        }

        System.out.println("Graph size after simplification: " + g.adjacencyList.size());
    }

    //I could probably bake this at runtime to help performance
    public static ArrayList<TerrainSection> loadFromImage(World world, BufferedImage img, float scale, float smoothness) {
        ArrayList<TerrainSection> path = new ArrayList<TerrainSection>();
        CollisionImageGraph graph = buildGraph(img, scale);

        simplifyGraph(graph, smoothness);

        //Now that we have a graph with edges along the boundary of our shape,
        //we do a dfs to find the connected components and build the geometry

        Stack<Vector2> toVisit = new Stack<Vector2>();
        Set<Vector2> visited = new HashSet<Vector2>();

        while (visited.size() < graph.adjacencyList.size()) {

            ArrayList<Vector2> pathVerts = new ArrayList<Vector2>();

            Vector2 next = null;

            for (Vector2 v : graph.vertices()) {
                if (!visited.contains(v)) {
                    next = v;
                    break;
                }
            }

            assert next != null;
            toVisit.add(next);

            while (!toVisit.isEmpty()) {
                Vector2 v = toVisit.pop();
                visited.add(v);
                pathVerts.add(v);

                for (CollisionImageGraph.Edge e : graph.adj(v)) {
                    if (!visited.contains(e.other(v)) && !toVisit.contains(e.other(v))) toVisit.add(e.other(v));
                }
            }

            pathVerts.add(pathVerts.get(0)); //We assume that they will always be closed paths

            Collections.addAll(path, generatePath(world, pathVerts.toArray(new Vector2[pathVerts.size()]), 0.1f, 0, 0, 100));
        }

        return path;
    }
}
