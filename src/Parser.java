import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class Parser {
    /**
     * Convert String to Node
     * @param name of the item
     * @return the Node of the string
     */
    private Node convert2Node(HashMap<String, Node> name2node, String name) {
        if (name.equals("null")) {
            return null;
        } else {
            name = name.trim();
            if (name2node.containsKey(name)) {
                return name2node.get(name);
            } else {
                Node node = new Node(name);
                name2node.put(name, node);
                return node;
            }
        }
    }

    public List<HashSet<ArrayList<String>>> parse(List<File> files) {
        List<HashSet<ArrayList<String>>> result = new ArrayList<>();
        for (File file : files) {
            result.add(parse(file));
            System.out.println(file.getName()+ " added!");
        }
        return result;
    }

    public HashSet<ArrayList<String>> parse(File file) {
        HashSet<ArrayList<String>> result = new HashSet<>();
        HashSet<Node> rootSet = new HashSet<>();
        HashMap<String, Node> name2node = new HashMap<>();

        // Read json into a string
        String jsonData = "";
        String lineText;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((lineText = br.readLine()) != null) {
                jsonData += lineText;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Parse the json
        JSONObject jsonObject = new JSONObject(jsonData);
//        JSONArray flowArray = new JSONArray(jsonData);
        JSONArray flowArray = jsonObject.getJSONArray("Flow");
        for (int i = 0; i < flowArray.length(); i++) {
            String name = flowArray.getJSONObject(i).getString("Name");
            String preName = flowArray.getJSONObject(i).getString("preName");
            Node thisNode = convert2Node(name2node, name);
            Node preNode = convert2Node(name2node, preName);
            assert thisNode != null;
            if (preNode == null) {
                rootSet.add(thisNode);
            } else {
                preNode.addNext(thisNode);
            }
        }

        // Get all path from root node
        for (Node root : rootSet) {
            Stack<Node> nodeStack = new Stack<>();
            HashSet<ArrayList<Node>> pathSet2 = new HashSet<>();
            boolean noLoop = root.toStack(nodeStack, pathSet2);
            if (noLoop) {
                for (ArrayList<Node> path : pathSet2) {
                    ArrayList<String> itemList = new ArrayList<>();
                    for (Node node :
                            path) {
                        itemList.add(node.name);
                    }
                    result.add(itemList);
                }
            } else {
                System.err.println("Loop in "+file.getName());
            }

        }
        return result;
    }

    class Node {
        private ArrayList<Node> nextList = new ArrayList<>();
        private String name;

        Node(String name) {
            this.name = name;
        }

        void addNext(Node n) {
            this.nextList.add(n);
        }


        boolean toStack(Stack<Node> nodeStack, HashSet<ArrayList<Node>> result) {
            if (nodeStack.contains(this)) {
                return false;
            } else {
                nodeStack.push(this);
                if (this.nextList.isEmpty()) {
                    // Is leaf node
                    result.add(new ArrayList<>(nodeStack));
                    nodeStack.pop();
                    return true;
                } else {
                    boolean noLoop = true;
                    for (Node next : this.nextList) {
                        noLoop &= next.toStack(nodeStack, result);
                    }
                    nodeStack.pop();
                    return noLoop;
                }
            }
        }


        HashSet<ArrayList<Node>> getAllPath() {
            HashSet<ArrayList<Node>> result = new HashSet<>();
            if (this.nextList.isEmpty()) {
                // Is leaf node
                ArrayList<Node> arrayList = new ArrayList<>();
                arrayList.add(this);
                result.add(arrayList);
            } else {
                for (Node n : this.nextList) {
                    HashSet<ArrayList<Node>> subset = n.getAllPath();
                    for (ArrayList<Node> arrayList :
                            subset) {
                        arrayList.add(0, this);
                        result.add(arrayList);
                    }
                }
            }
            return result;
        }
    }
}
