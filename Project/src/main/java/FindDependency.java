import java.io.*;
import java.util.ArrayList;

public class FindDependency {
    private Graph_generation gg = new Graph_generation();
    private String changeInfoPath;
    private String outputTxT[] = new String[]{"selection-class.txt", "selection-method.txt"};

    public Graph_generation getGg() {
        return gg;
    }

    public FindDependency(String changeInfoPath) {
        this.changeInfoPath = changeInfoPath;
    }

    public void handle(int type) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(changeInfoPath));
        BufferedWriter w = new BufferedWriter(new FileWriter(outputTxT[type]));
        ArrayList<String> denpendency = new ArrayList<>();
        String line;
        while ((line = r.readLine()) != null) {
            String content[] = line.split(" ");
            ArrayList<String> route = new ArrayList<String>();
            route.add(content[0] + " " + content[1]);
            if (type==0)
                findAllDependency_class(denpendency, content[0], route);

            else if (type==1)
                findAllDependency_method(denpendency, content, route);
        }
        r.close();
        if(type==0) {
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0; i < gg.callers.size(); i++) {
                String cs = gg.callers.get(i)[0] + " " + gg.callers.get(i)[1];
                if (!result.contains(cs) && denpendency.contains(gg.callers.get(i)[0])&&gg.callers_annotation.get(i).contains("Lorg/junit/Test"))
                    result.add(cs);
            }
            for(String i:result)
                w.write(i+"\n");

        }
        if(type==1)
            for (String i : denpendency)
                w.write(i + "\n");
        w.close();
        System.out.println("Done");
    }

    //递归寻找方法级依赖
    public void findAllDependency_method(ArrayList<String> dependency, String[] cs, ArrayList<String> route) {
        for (int i = 0; i < gg.callees.size(); i++) {
            boolean haveFound = true;
            for (int j = 0; j < cs.length; j++) {
                if (!cs[j].equals(gg.callees.get(i)[j])) {
                    haveFound = false;
                    break;
                }
            }

            if (haveFound) {
                String caller = gg.callers.get(i)[0] + " " + gg.callers.get(i)[1];
                if (!route.contains(caller) && !dependency.contains(caller)) {
                    if (gg.callers_annotation.get(i).contains("Lorg/junit/Test"))
                        dependency.add(caller);

                    route.add(caller);
                    findAllDependency_method(dependency, gg.callers.get(i), route);
                    route.remove(route.size() - 1);
                    }
                }
        }
    }
    //递归寻找类级依赖
    public void findAllDependency_class(ArrayList<String> dependency, String c, ArrayList<String> route) {
        for (int i = 0; i < gg.callees.size(); i++) {
            if(c.equals(gg.callees.get(i)[0])){
                if(!route.contains(gg.callers.get(i)[0])&&!dependency.contains(gg.callers.get(i)[0])){
                    if (gg.callers_annotation.get(i).contains("Lorg/junit/Test"))
                        dependency.add(gg.callers.get(i)[0]);
                    route.add(gg.callers.get(i)[0]);
                    findAllDependency_class(dependency, gg.callers.get(i)[0], route);
                    route.remove(route.size() - 1);
                }
            }
        }
    }
}

