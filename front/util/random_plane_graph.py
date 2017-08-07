import math, sys, random
### paramerters
min_node_and_node_dist = 5    # ノード・ノード間の最小距離。大きくすると終了が遅くなる
min_node_and_edge_dist = 2    # ノード・エッジ間の最小距離。大きくすると終了が顕著に遅くなる
small_world_factor = 15       # これが大きいと遠いノード同士にエッジが貼られない。小さくすると終了が遅くなる。
### END: parameters

nodes = []
edges = []
mines = []
nodeId = 0
maxX = 1000
maxY = 1000

class Node:
    def __init__(self, x, y):
        global nodeId
        self.x = x
        self.y = y
        self.nodeId = nodeId
    def __eq__(self, other):
        eps = 1e-8
        return abs(self.x - other.x) < eps and abs(self.y - other.y) < eps
    def dist(self, other):
        return math.sqrt((self.x - other.x)**2 + (self.y -  other.y)**2)
    def distFromEdge(self, edge):
        node1, node2 = edge.source, edge.target
        dx0, dy0 = self.x - node1.x, self.y - node1.y
        dx1, dy1 = node2.x - node1.x, node2.y - node1.y

        d = abs(dx0*dy1 - dx1*dy0)
        l = node1.dist(node2)
        return d/l
    def __repr__(self):
        return "%d:(%.5f, %.5f)" % (self.nodeId, self.x, self.y) 

class Edge:
    def __init__(self, node0, node1):
        self.source = node0
        self.target = node1
    def __repr__(self):
        return "<%d:(%.5f, %.5f) <=> %d:(%.5f, %.5f)" % (
                self.source.nodeId, self.source.x, self.source.y, self.target.nodeId, self.target.x, self.target.y
                ) 
    def isIntersect(self, other):
        node0, node1, node2, node3 = self.source, self.target, other.source, other.target
        x0, y0 = node0.x, node0.y
        x1, y1 = node1.x, node1.y
        x2, y2 = node2.x, node2.y
        x3, y3 = node3.x, node3.y

        dx0, dy0 = x1 - x0, y1 - y0
        dx1, dy1 = x3 - x2, y3 - y2
    
        s = (y0-y2)*dx1 - (x0-x2)*dy1
        sm = dx0*dy1 - dy0*dx1
        if s < 0:
            s = -s
            sm = -sm
        t = (y2-y0)*dx0 - (x2-x0)*dy0
        tm = dx1*dy0 - dy1*dx0
        if t < 0:
            t = -t
            tm = -tm
        if 0 <= s <= max(sm, 0) and 0 <= t <= max(tm, 0):
            ov = dx0*dy1 - dy0*dx1
            if ov == 0:
                r0 = dx0**2 + dy0**2
                r1 = (x2-x0)*dx0 + (y2-y0)*dy0
                r2 = (x3-x0)*dx0 + (y3-y0)*dy0
                if r1 > r2: r1, r2 = r2, r1
                if r2 < 0 or r0 < r1:
                    return False
                else:
                    return True
            else:
                return True
        else:
            return False

def existTooNearPoint(node0, d):
    for node1 in nodes:
        if node0.dist(node1) < d:
            return True
    return False

def existTooNearEdge(node, d):
    for edge in edges:
        if node.distFromEdge(edge) < d:
            return True
    return False

def formAsJsonFormat(nodes, edges, mines):
    sites = [] 
    for node in nodes:
        sites.append('{"id": %d, "x": %.5f, "y": %.5f}' % (node.nodeId, node.x, node.y))
    rivers = [] 
    for edge in edges:
        rivers.append('{"source": %d, "target": %d}' % (edge.source.nodeId, edge.target.nodeId))

    sites = ", ".join(sites)
    rivers = ", ".join(rivers)
    mines = ", ".join(map(str, mines))
    json = '{"sites": [%s], "rivers":[%s], "mines":[%s]}' % (sites, rivers, mines)
    return json

def usage():
    print("usage: python3 random_plane_grapy.py <num node> <num mines> <sparse degree(0 - 100)>")

def main():
    global nodes, edges, mines, nodeId
    argv = sys.argv 
    if (len(argv) != 4):
        usage()
        return

    if (not argv[1].isdigit() or not argv[2].isdigit() or not argv[3].isdigit()):
        usage()
        return

    num_nodes = int(argv[1])
    num_mines = int(argv[2])
    sparse_degree = int(argv[3])
    diagonalDist = Node(-maxX, maxY).dist(Node(maxX, -maxY));

    nodes.append(Node(0.0, 0.0)); nodeId += 1
    nodes.append(Node(maxX/10.0, 0.0)); nodeId += 1
    edges.append(Edge(nodes[0], nodes[1]))
    try:
        while len(nodes) < num_nodes:
            base = random.choice(nodes)
            theta = math.radians(random.uniform(0, 360))
            coeff = random.uniform(5, 100)
            dx, dy = coeff*math.sin(theta), coeff*math.cos(theta)
            if not (-maxX <= base.x + dx <= maxX and -maxY <= base.y + dy <= maxY): continue
            node = Node(base.x + dx, base.y + dy)
            if existTooNearPoint(node, min_node_and_node_dist):
                continue
            if existTooNearEdge(node, min_node_and_edge_dist):
                continue
            n_edges = edges[:]
            for node1 in nodes:
                if node.dist(node1) > diagonalDist/small_world_factor:
                    continue
                edge = Edge(node, node1)
                for edge1 in edges:
                    if node1 == edge1.source or node1 == edge1.target:
                        continue
                    if edge.isIntersect(edge1):
                        break
                else:
                    if (random.randint(0, 100) >= sparse_degree):
                        n_edges.append(edge)
            nodes.append(node)
            nodeId += 1
            print(nodeId, file = sys.stderr)
            edges = n_edges

        mines = random.sample(range(num_nodes), num_mines)

        json = formAsJsonFormat(nodes, edges, mines)
        with open("randomPlane_N%d_M%d_S%d.json" % (num_nodes, num_mines, sparse_degree), "w") as f:
            f.write(json)
        print(json)
    except KeyboardInterrupt:
        print("KeyboardInterrupt!!! Print half-way result...", file = sys.stderr)
        print(formAsJsonFormat(nodes, edges, mines), file = sys.stderr)
        

if __name__ == "__main__":
    main()
