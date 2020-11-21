import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Graph_generation {
    AnalysisScope scope;
    String[] outputDot=new String[]{"class.dot" , "method.dot"};
    String[] outputType=new String[]{"class","method"};
    ArrayList<String[]> callees=new ArrayList<>();
    ArrayList<String[]> callers=new ArrayList<>();
    ArrayList<ArrayList<String>> callers_annotation=new ArrayList<>();


    public void setScope(String project_target) throws InvalidClassFileException, IOException {
        ClassLoader classLoader= Graph_generation.class.getClassLoader();
        scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), classLoader);
        ArrayList<String> classpath=new ArrayList<String>();
        getClasses(project_target,classpath);
        for(int i=0;i<classpath.size();i++)
            scope.addClassFileToScope(ClassLoaderReference.Application,new File(classpath.get(i)));
    }

    //遍历target下所有的class文件
    void getClasses(String project_target,ArrayList<String> classpath){
        File file=new File(project_target);
        if(file.isDirectory()){
            File[] files=file.listFiles();
            for(int i=0;i<files.length;i++){
                if(files[i].isDirectory())
                    getClasses(files[i].getPath(),classpath);
                else if(files[i].getName().endsWith(".class"))
                    classpath.add(files[i].getPath());
            }
        }
    }

    //生成依赖图
    public void getGraph() throws ClassHierarchyException, CancelException, IOException {
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);
        int count=0;
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String cs[]=new String[2];
                    cs[0]= method.getDeclaringClass().getName().toString();
                    cs[1]= method.getSignature();
                    Iterator<CGNode> caller = cg.getPredNodes(node);//获得图中所有的前序节点
                    while (caller.hasNext()) {
                        CGNode n = caller.next();
                        ShrikeBTMethod n_method = (ShrikeBTMethod)n.getMethod();
                        if ("Application".equals(n_method.getDeclaringClass().getClassLoader().toString())) {
                            String n_cs[]=new String[2];
                            n_cs[0] = n_method.getDeclaringClass().getName().toString();
                            n_cs[1] = n_method.getSignature();
                            Collection<com.ibm.wala.types.annotations.Annotation> annotations=n_method.getAnnotations();
                            callers_annotation.add(new ArrayList<String>());//用于判断调用者是否为测试方法
                            for(Annotation o:annotations)
                                callers_annotation.get(count).add(o.getType().getName().toString());

                            count++;
                            callers.add(n_cs);
                            callees.add(cs);
                            }
                        }
                    }
                }
            }
    }

    //输出dot文件(正常流程中未调用)
    public void outputDot(String name,int type) throws IOException {
        BufferedWriter buf=new BufferedWriter(new FileWriter(outputDot[type]));
        buf.write("digraph "+name+"_"+outputType[type]+"{\n");
        for(int i=0;i<callers.size();i++){
            String line="\""+ callees.get(i)[type]+"\""+" -> "+"\""+callers.get(i)[type]+"\""+";";
            buf.write(line+"\n");
        }
        buf.write("}");
        buf.close();
    }
}
