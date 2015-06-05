package me.justin.culminating;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import me.justin.culminating.terrain.Metaball;
import me.justin.culminating.terrain.TerrainSection;
import me.justin.culminating.terrain.TerrainSectionPath;
import me.justin.culminating.terrain.TerrainSectionPolygon;

/**
 * Created by justin on 05/04/15.
 */
public class TerrainUtils {

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

        public void add(Vector2 a, Vector2 b, int weight) {
            if (!adjacencyList.containsKey(a)) adjacencyList.put(a, new LinkedList<Edge>());
            if (!adjacencyList.containsKey(b)) adjacencyList.put(b, new LinkedList<Edge>());

            //Ensure that there are no duplicate edges
            if (a.equals(b)) return;
            for (Edge e : adj(a)) {
                if (e.a.equals(a) && e.b.equals(b)
                        || e.a.equals(b) && e.b.equals(a)) return;
            }
            for (Edge e : adj(b)) {
                if (e.a.equals(b) && e.b.equals(a)
                        || e.a.equals(a) && e.b.equals(b)) return;
            }

            adjacencyList.get(a).add(new Edge(a,b, weight));
            adjacencyList.get(b).add(new Edge(b,a, weight));
        }

        public Iterable<Edge> adj(Vector2 v) {
            if (adjacencyList.containsKey(v)) return adjacencyList.get(v);
            else return new ArrayList<Edge>();
        }

        public Iterable<Vector2> vertices() {
            return adjacencyList.keySet();
        }
    }

    //god this is awful
    private static CollisionImageGraph buildGraph(Pixmap img, float scale) {
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

    private static boolean isBoundary(int x, int y, Pixmap img) {

        if (img.getPixel(x,y) != 0x000000FF) return false; //Not black

        for (int nx = MathUtils.clamp(x-1, 0, img.getWidth()-1); nx<= MathUtils.clamp(x+1, 0, img.getWidth()-1); nx++) {
            for (int ny = MathUtils.clamp(y-1, 0, img.getHeight()-1); ny<= MathUtils.clamp(y+1, 0, img.getHeight()-1); ny++) {
                //Ignore diagonals when checking boundaries
                if (nx == x+1 && ny == y+1 || nx == x-1 && ny == y+1
                        || nx == x-1 && ny == y-1 || nx == x+1 && ny == y-1) continue;
                if (nx == x && ny == y) continue;

                if (img.getPixel(nx, ny) != 0x000000FF) return true; //not black
            }
        }

        return false;
    }

    //Pixmaps have origin in top left, libgdx and box2d in bottom left
    private static float getWorldY(float y, Pixmap img) {
        return img.getHeight()-y-1;
    }

    private static ArrayList<ArrayList<Vector2>> getPaths(CollisionImageGraph graph) {
        ArrayList<ArrayList<Vector2>> paths = new ArrayList<ArrayList<Vector2>>();

        //TODO replace the building of the graph with an implicit graph that directly checks the image for performance

        //Now that we have a graph with edges along the boundary of our shape,
        //we do a dfs to find the connected components and build the geometry
        //The order should be fine because each vertex only has 2 edges (Except for ones near corners, but whatever)
        Stack<Vector2> toVisit = new Stack<Vector2>();
        Set<Vector2> visited = new HashSet<Vector2>();

        while (visited.size() < graph.adjacencyList.size()) {

            ArrayList<Vector2> path = new ArrayList<Vector2>();

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
                path.add(v);

                for (CollisionImageGraph.Edge e : graph.adj(v)) {
                    if (!visited.contains(e.other(v)) && !toVisit.contains(e.other(v))) toVisit.add(e.other(v));
                }
            }

            path.add(path.get(0)); //We assume that they will always be closed paths

            if (path.size() > 2) paths.add(path);
        }

        return paths;
    }

    private static float dist2PointLine(Vector2 p, Vector2 v, Vector2 w) {
        // http://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
        // Return minimum distance between line segment vw and point p
        float l2 = v.dst2(w);
        if (l2 == 0) return p.dst(v);   // v == w case

        // Consider the line extending the segment, parameterized as v + t (w - v).
        // We find projection of point p onto the line.
        // It falls where t = [(p-v) . (w-v)] / |w-v|^2
        float t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2;
        if (t < 0) return p.dst(v);       // Beyond the 'v' end of the segment
        else if (t > 1) return p.dst(w);  // Beyond the 'w' end of the segment

        Vector2 projection = new Vector2(v.x + t * (w.x - v.x),
                v.y + t * (w.y - v.y));  // Projection falls on the segment
        return p.dst(projection);
    }

    //http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
    private static ArrayList<Vector2> simplifyLine(ArrayList<Vector2> line, float smoothness) {
        ArrayList<Vector2> list = new ArrayList<Vector2>();
        list.add(line.get(0));
        list.addAll(simplifyLineSection(line, 1, line.size() - 2, smoothness));
        list.add(line.get(line.size()-1));
        return list;
    }

    private static ArrayList<Vector2> simplifyLineSection(ArrayList<Vector2> line, int low, int high, float smoothness) {

        ArrayList<Vector2> newPoints = new ArrayList<Vector2>();

        //Find the point in the line segment furthest from the line formed by the endpoints of this section
        int maxPointIndex = -1;
        float maxDist = -1;

        for (int i=low+1; i<=high-1; i++) {
            float dist = dist2PointLine(line.get(i), line.get(low), line.get(high));
            if (maxPointIndex == -1 || dist > maxDist) {
                maxDist = dist;
                maxPointIndex = i;
            }
        }

        if (maxDist < smoothness || maxPointIndex == -1) {
            //We can just use the line formed by the two endpoints
            newPoints.add(line.get(low));
            newPoints.add(line.get(high));
        }
        else {
            newPoints.addAll(simplifyLineSection(line, low, maxPointIndex, smoothness));
            newPoints.remove(newPoints.size()-1); //Prevent a duplicate point from maxPoint
            newPoints.addAll(simplifyLineSection(line, maxPointIndex, high, smoothness));
        }

        return newPoints;
    }

    public static ArrayList<TerrainSection> getTerrainFromImage(World world, Pixmap img, float scale, float smoothness) {
        return getTerrainFromGraph(world, buildGraph(img, scale), smoothness);
    }

    //I could probably bake this at runtime to help performance
    private static ArrayList<TerrainSection> getTerrainFromGraph(World world, CollisionImageGraph graph, float smoothness) {
        ArrayList<TerrainSection> terrain = new ArrayList<TerrainSection>();

        int beforeSize = 0, afterSize = 0;

        for (ArrayList<Vector2> path : getPaths(graph)) {
            beforeSize += path.size();
            ArrayList<Vector2> simplified = simplifyLine(path, smoothness);
            afterSize += simplified.size();
            terrain.add(new TerrainSectionPath(world, 0, 0, simplified.toArray(new Vector2[simplified.size()]), 0.1f, 100));
        }

        System.out.println("Collision geometry verts before simplification: " + beforeSize);
        System.out.println("Collision geometry verts after simplification: " + afterSize);
        System.out.println("Smoothness: " + smoothness + "; Reduction: " + (float)afterSize/beforeSize * 100 + "%");

        return terrain;
    }

    public static ArrayList<TerrainSection> loadFromMetaballs(World world, boolean[][] scalarField, float scale, float smoothness) {
        return getTerrainFromGraph(world, buildGraphFromScalarField(scalarField, scale), smoothness);
    }

    private static CollisionImageGraph buildGraphFromScalarField(boolean[][] scalarField, float scale) {
        CollisionImageGraph graph = new CollisionImageGraph();

        for (int x=0; x<scalarField.length; x++) {
            for (int y=0; y<scalarField[x].length; y++) {

                if (isScalarBoundary(x,y,scalarField)) {
                    Vector2 curr = new Vector2((x)*scale, (scalarField[x].length-(y))*scale);

                    for (int nx = MathUtils.clamp(x-1, 0, scalarField.length-1); nx<= MathUtils.clamp(x+1, 0, scalarField.length-1); nx++) {
                        for (int ny = MathUtils.clamp(y-1, 0, scalarField[0].length-1); ny<= MathUtils.clamp(y+1, 0, scalarField[0].length-1); ny++) {
                            if (nx == x && ny == y) continue;

                            if (isScalarBoundary(nx,ny,scalarField)) {
                                graph.add(curr, new Vector2((nx)*scale, (scalarField[nx].length-(ny))*scale), 1);
                            }
                        }
                    }
                }
            }
        }

        return graph;
    }

    private static boolean isScalarBoundary(int x, int y, boolean[][] field) {

        if (!field[x][y]) return false;

        for (int nx = MathUtils.clamp(x-1, 0, field.length-1); nx<= MathUtils.clamp(x+1, 0, field.length-1); nx++) {
            for (int ny = MathUtils.clamp(y-1, 0, field[nx].length-1); ny<= MathUtils.clamp(y+1, 0, field[nx].length-1); ny++) {
                //Ignore diagonals when checking boundaries
                if (nx == x+1 && ny == y+1 || nx == x-1 && ny == y+1
                        || nx == x-1 && ny == y-1 || nx == x+1 && ny == y-1) continue;
                if (nx == x && ny == y) continue;

                if (!field[nx][ny]) return true;
            }
        }

        return false;
    }

    public static boolean[][] generateScalarField(Metaball[] balls, int startX, int startY, int endX, int endY, float scale) {
        boolean[][] values = new boolean[endX-startX][endY-startX];

        for (int x=startX; x<endX; x++) {
            for (int y=startY; y<endY; y++) {
                boolean value = getValueAt(x*scale,y*scale,balls);

                values[x-startX][y-startY] = value;
            }
        }

        return values;
    }

    public static Metaball[] getBalls() {
        Metaball[] blobs = new Metaball[5];

        int width = 1000, height = 1000;

        for (int i=0; i<blobs.length; i++) {

            Metaball ball = new Metaball();
            ball.x = (float) Math.random()*width;
            ball.y = (float) Math.random()*height;
            ball.radius = (float) Math.random()*300 + 60;

            blobs[i] = ball;
        }

        return blobs;
    }

    private static boolean getValueAt(float x, float y, Metaball[] balls) {
        float value = 0;
        float g = 3;
        int threshold = 5;

        for (Metaball b : balls) {
            //Try to speed it up a bit by ignoring far away balls (TODO replace with bsp tree)
//            if ((b.x-x)*(b.x-x) + (b.y-y)*(b.y-y) > (b.radius + 1000)*(b.radius + 1000)) continue;

            value += Math.pow(b.radius, g) / Math.pow(Math.sqrt((b.x - x)*(b.x-x) + (b.y-y)*(b.y-y)), g);
            if (value > threshold) break;
        }

        if (value > threshold) return true;
        else return false;
    }
}
