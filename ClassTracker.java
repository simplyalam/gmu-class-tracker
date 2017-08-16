import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;
import java.io.*;
import java.util.*;

public class ClassTracker {
    private String crn;
    public int cap;
    public int remain;
    public int waitCap;
    public int waitRemain;
    public String classname;

    public ClassTracker(String crn) {
        this.crn = crn;
    }

    public String getInfo() {
        String text = "Error";
        try {
            Document doc = Jsoup.connect("https://patriotweb.gmu.edu/pls/prod/bwckschd.p_disp_detail_sched?term_in=201770&crn_in=" + crn).get();
            // Using CSS like selector syntax, the seat values are retrieved.
            Elements seats = doc.select("table[summary*=seat]  tr:gt(0) td:matches(\\d)");
            // The class information is retrieved.
            // (Class name, CRN, course #, section #)
            Elements classinfo = doc.select("th:contains(" + crn + ")");

            // Class capacity, seats remaining, waitlist capacity,
            // waitlist seats remaining, and class info are assigned to variables.
            cap = Integer.parseInt(seats.get(0).html());
            remain = Integer.parseInt(seats.get(2).html());
            waitCap = Integer.parseInt(seats.get(3).html());
            waitRemain = Integer.parseInt(seats.get(5).html());
            classname = classinfo.get(0).html().replace("<br><br>", "");

            text = classname + "\nClass Size: " + cap + "\nSeats Remaining: " + remain + "\nWaitlist Size: " + waitCap + "\nWaitlist Remaining: " + waitRemain;
            return text;
        }
        catch (IndexOutOfBoundsException e) {
            return "Class not found";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
}
