package package_view;

import package_card.Condition;

import java.util.Scanner;


public abstract class View {

    private static final Scanner sc = new Scanner(System.in);

    protected void show() {
        int action;
        do {
            menu();
            System.out.print("SELECT: ");
            action = getInteger();
            try {
                actions(action);
            } catch (ViewException e) {
                throw new RuntimeException(e);
            }
        } while (action != 0);
    }

    protected void actions(int action) throws ViewException {

    }

    protected void menu() {

    }

    protected void errorAction() {
        System.out.println("Warning - action not found");
    }

    protected String getInput() {
        return sc.next();
    }

    protected static Integer getInteger() {
        return sc.nextInt();
    }

    protected static Condition getCondition() throws Exception {
        String s = sc.next();
        return switch (s) {
            case "NM" -> Condition.NM;
            case "M" -> Condition.M;
            case "EX" -> Condition.EX;
            case "GO" -> Condition.GO;
            case "LP" -> Condition.LP;
            case "PL" -> Condition.PL;
            case "PO" -> Condition.PO;
            default -> throw new Exception();
        };
    }

}
