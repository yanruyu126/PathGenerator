import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Map {
    private Set<Item> items;
    private int width, height;
    private int bucketSize;
    private List<Point> path;
    private Point initPos;
    private Set<Point> ps;
    private double ratio;

    Map(BufferedImage image) {
        width= image.getWidth();
        height= image.getHeight();
        ratio= (double) R.Frame_Size / (double) Math.max(width, height);
        items= new HashSet<>();
        initPos= new Point(0, 0);
        path= new LinkedList<>();
        path.add(initPos);
        ps= readPoints(image);
        Set<Point> itemPoints= readPoints(image);
        loadItems(itemPoints);
        calculatePath();
    }

    private Set<Point> readPoints(BufferedImage image) {
        Set<Point> points= new HashSet<>();

        for (int x= 0; x < width; x++ ) {
            for (int y= 0; y < height; y++ ) {
                int color= image.getRGB(x, y);
                int red= (color & 0x00ff0000) >> 16;
                int green= (color & 0x0000ff00) >> 8;
                int blue= color & 0x000000ff;
                double darkness= 1 - (0.2126 * red + 0.7152 * green + 0.0722 * blue) / 255;
                if (darkness > R.DARKNESS) {
                    points.add(new Point(x, y));
                }
            }
        }

        return points;
    }

    private Collection<Set<Point>> clusterizePoints(Set<Point> points) {
        HashMap<Point, Set<Point>> buckets= new HashMap<>();
        boolean loaded;

        for (Point p : points) {
            loaded= false;

            Set<Point> centerPoints= buckets.keySet();

            for (Point centerP : centerPoints) {
                if (p.distance(centerP) < R.ITEM_SIZE / ratio) {
                    Set<Point> bucket= buckets.get(centerP);
                    bucket.add(p);
                    loaded= true;
                    break;
                }
            }

            if (!loaded) {
                Set<Point> newBucket= new HashSet<>();
                newBucket.add(p);
                buckets.put(p, newBucket);
            }
        }

        bucketSize= points.size() / buckets.size() / 3;

        Collection<Set<Point>> result= new HashSet<>();
        for (Set<Point> bucket : buckets.values()) {
            Set<Point> new_bucket= recenterCluster(bucket);
            if (new_bucket.size() > bucketSize) result.add(new_bucket);

        }

        return result;
    }

    private Set<Point> recenterCluster(Set<Point> points) {
        int accX= 0;
        int accY= 0;

        for (Point p : points) {
            accX+= p.x;
            accY+= p.y;
        }

        Point center= new Point(accX / points.size(), accY / points.size());
        Set<Point> result= new HashSet<>();

        for (Point p : points) {
            if (p.distance(center) < R.ITEM_SIZE / 2 / ratio) {
                result.add(p);
            }
        }
        return result;
    }

    private void loadItems(Set<Point> points) {
        Collection<Set<Point>> clusters= clusterizePoints(points);

        int minX, maxX, minY, maxY, centerX, centerY, radius, itemWidth, itemHeight;

        for (Set<Point> cluster : clusters) {
            minX= width;
            minY= height;
            maxX= 0;
            maxY= 0;
            for (Point p : cluster) {
                minX= Math.min(minX, p.x);
                minY= Math.min(minY, p.y);
                maxX= Math.max(maxX, p.x);
                maxY= Math.max(maxY, p.y);
            }
            centerX= (minX + maxX) / 2;
            centerY= (minY + maxY) / 2;
            itemWidth= maxX - minX;
            itemHeight= maxY - minY;
            radius= (int) (Math.max(itemWidth, itemHeight) * 1.2 / 2);
            if (itemWidth > R.ITEM_SIZE / 3 / ratio && itemHeight > R.ITEM_SIZE / 3 / ratio) {
                items.add(new Item(centerX, centerY, radius));
            }
        }
    }

    private void permute(List<Item> items, Set<List<Item>> acc, int k) {
        for (int i= k; i < items.size(); i++ ) {
            java.util.Collections.swap(items, i, k);
            permute(items, acc, k + 1);
            java.util.Collections.swap(items, k, i);
        }
        if (k == items.size() - 1) {
            acc.add(new LinkedList<>(items));
        }
    }

    private List<Item> visitPlan() {
        List<Item> result= new LinkedList<>();
        List<Item> itemsCopy= new LinkedList<>(items);
        Set<List<Item>> permutations= new HashSet<>();
        permute(itemsCopy, permutations, 0);

        int minDis= Integer.MAX_VALUE;
        int dis= 0;
        Point currentP;

        for (List<Item> order : permutations) {
            currentP= initPos;
            for (Item i : order) {
                dis+= currentP.distance(i.center);
                currentP= i.center;
            }
            if (dis < minDis) {
                minDis= dis;
                result= order;
            }
            dis= 0;
        }

//        while (!itemsCopy.isEmpty()) {
//            minDis= width + height;
//            next= null;
//            for (Item i : itemsCopy) {
//                dis= myPosition.distance(i.center);
//                if (dis < minDis) {
//                    minDis= dis;
//                    next= i;
//                }
//            }
//            result.add(next);
//            itemsCopy.remove(next);
//            myPosition= next.center;
//        }

        for (Item i : result) {
            System.out.println(i.center);
        }

        return result;
    }

    private void calculatePath() {
        List<Item> orderedItems= visitPlan();
        Point pos= initPos;
//        for (Item item : orderedItems) {
//            System.out.println(item.center.x);
//            System.out.println(item.center.y);
//            System.out.println();
//            double k= (double) (pos.y - item.center.y) / (double) (pos.x - item.center.x);
//            double l= pos.distance(item.center) - item.radius;
//            Point next= endPoint(pos, k, l);
//            path.add(next);
//            pos= endPoint(next, -1 / k, -100);
//            path.add(pos);
//        }
        for (int i= 0; i < orderedItems.size() - 1; i++ ) {
            Item item1= orderedItems.get(i);
            Item item2= orderedItems.get(i + 1);
            pos= checkItem(pos, item1);
            pos= detour(pos, item1, item2);
        }
//        Point p= checkItem(pos, orderedItems.get(0));
//        System.out.println(orderedItems.get(0).center.x);
//        System.out.println(orderedItems.get(0).center.y);
//        System.out.println(orderedItems.get(0).radius);
//        System.out.println();
//        System.out.println(p.x);
//        System.out.println(p.y);
    }

    public List<Point> backToOrigin() {
        return null;
    }

    private Point checkItem(Point pos, Item item) {
        double k= (double) (pos.y - item.center.y) / (double) (pos.x - item.center.x);
        double l= pos.distance(item.center) - item.radius;
        Point next= endPoint(pos, k, item.center.x > pos.x ? l : -l);
        path.add(next);
        return next;
    }

    private Point detour(Point pos, Item item1, Item item2) {
//        double alpha= Math.acos(Math.cos(item2.center.distance(item1.center) / item1.radius));
//        double beta= (Math.PI - alpha) / 2;
//        double detourLen= item1.radius / Math.cos(beta);
//        double detourK= Math.tan(Math.atan(gradient(item1.center, item2.center)) + alpha + beta);
//        Point detourP= endPoint(item1.center, detourK, detourLen);
//        path.add(detourP);
//        return detourP;
        double a= pos.x;
        double b= pos.y;
        double c= item1.center.x;
        double d= item1.center.y;
        double e= item2.center.x;
        double f= item2.center.y;
        double r= item1.radius;

        Set<Point> intersections= new HashSet<>();

        for (double x= c - r; x <= c + r; x++ ) {
            double y1= Math.sqrt(r * r - (x - c) * (x - c)) + d;
            double y2= (r * r - (e - c) * (x - c)) / (f - d) + d;
            if (Math.abs(y1 - y2) < 20) {
                intersections.add(new Point((int) x, (int) y1));
            }
        }

        double k1= (c - a) / (b - d);
        double b1= b - k1 * a;
        Set<Point> turningPoints= new HashSet<>();

        for (Point p : intersections) {
            double k2= (f - p.y) / (e - p.x);
            double b2= p.y - k2 * p.x;
            double tx= (b2 - b1) / (k1 - k2);
            double ty= tx * k1 + b1;
            turningPoints.add(new Point((int) tx, (int) ty));
        }

        turningPoints.add(endPoint(pos, k1, R.ITEM_SIZE / ratio));
        turningPoints.add(endPoint(pos, k1, -R.ITEM_SIZE / ratio));
        System.out.println(endPoint(pos, k1, R.ITEM_SIZE / ratio));
        System.out.println(endPoint(pos, k1, -R.ITEM_SIZE / ratio));
        System.out.println();

        Point next= null;
        double min_dis= R.Frame_Size;

        for (Point tp : turningPoints) {
            double dis= pos.distance(tp) + tp.distance(item2.center);
//            System.out.println(dis);
//            System.out.println();
            if (dis < min_dis) {
                min_dis= dis;
                next= tp;
            }
        }
//        System.out.println(next);

        path.add(next);
        return checkItem(next, item2);
    }

    private Point endPoint(Point pos, double k, double l) {
//        System.out.println("---");
//        System.out.println(pos);
//        System.out.println(k);
//        System.out.println(l);

        double a= Math.sqrt(l * l / (k * k + 1));
        double b= k * a;
        int x= (int) (pos.x + (l > 0 ? a : -a));
        int y= (int) (pos.y + (l > 0 ? b : -b));
//        System.out.println(new Point(x, y));
//        System.out.println("---");
        return new Point(x, y);
    }

//    private double gradient(Point p1, Point p2) {
//        return (double) (p1.y - p2.y) / (double) (p1.x - p2.x);
//    }

    public List<Point> getPath() {
        return path;
    }

    public void draw(Graphics2D g) {
        int x, y, d;
        for (Item item : items) {
            g.setColor(new Color(0, 100, 200));
            x= (int) ((item.center.x - item.radius) * ratio);
            y= (int) ((item.center.y - item.radius) * ratio);
            d= (int) (item.radius * 2 * ratio);
            g.fillOval(x, y, d, d);
        }
        for (Point p : path) {
            g.setColor(new Color(100, 100, 100));
            x= (int) (p.x * ratio);
            y= (int) (p.y * ratio);
            g.fillOval(x, y, 10, 10);
        }
        for (int i= 0; i < path.size() - 1; i++ ) {
            Point p1= path.get(i);
            Point p2= path.get(i + 1);
            x= (int) (p1.x * ratio);
            y= (int) (p1.y * ratio);
            int x2= (int) (p2.x * ratio);
            int y2= (int) (p2.y * ratio);
            g.drawLine(x, y, x2, y2);
        }
        for (Point p : ps) {
            g.setColor(new Color(100, 220, 250));
            x= (int) (p.x * ratio);
            y= (int) (p.y * ratio);
            g.fillOval(x, y, 5, 5);
        }
    }

    public class Item {

        int radius;
        Point center;

        Item(int x, int y, int radius) {
            center= new Point(x, y);
            this.radius= radius;
//            System.out.println(center.x);
//            System.out.println(center.y);
//            System.out.println(radius);
//            System.out.println();
        }
    }
}
