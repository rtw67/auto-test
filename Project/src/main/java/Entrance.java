public class Entrance {
    private static String INSTRCTION_ERROR = "UNKNOWN INSTRCTION";

    public static void main(String args[]) {
        if (args.length == 3) {
            if(!args[0].equals("-m")&&!args[0].equals("-c")){
                System.out.println(INSTRCTION_ERROR);
                return;
            }
            String project_target = args[1];
            String change_info = args[2];
            FindDependency fd = new FindDependency(change_info);
            try {
                fd.getGg().setScope(project_target);
                fd.getGg().getGraph();
                fd.handle(args[0].equals("-c")?0:1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println(INSTRCTION_ERROR);
    }
}
