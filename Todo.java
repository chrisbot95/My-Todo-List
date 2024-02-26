import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;

class Todo{

    private static Scanner scanner;
    private static File file_todo, file_completed, file_history, file_date, file_show, file_study, file_network, file_removed;
    private static String string_date_on_file, which_section;
    private static String string_date_today = LocalDate.now().toString();
    private static ArrayList<String> arraylist_todo = new ArrayList<String>();
    private static ArrayList<String> arraylist_history = new ArrayList<String>();
    private static ArrayList<String> arraylist_study, arraylist_network, arraylist_removed;
    private static int[] array_indexes = {0,0,0,0,0};
    private static String[] array_section_strings = {"Now", "Later", "Tomorrow", "Someday", "Maybe"};
    private static int int_completed;
    private static PrintWriter writer;
    private static boolean bool_show;

    public static void main(String[] args){
        Set_up_files();
        Create_todo_arraylist_from_file();
        arraylist_history = Create_arraylist_from_file(file_history);
        arraylist_removed = Create_arraylist_from_file(file_removed);
        int_completed = Integer.parseInt(Get_string_from_file(file_completed));
        bool_show = Boolean.parseBoolean(Get_string_from_file(file_show));
        Check_date();
        Print();
        User_input();
        Write_todo_file();
        Write_to_file(int_completed, file_completed);
        Write_to_file(string_date_today, file_date);
        Write_to_file(bool_show, file_show);
        Write_to_file(arraylist_history, file_history);
        Write_to_file(arraylist_removed, file_removed);
    }

    // ===== Main Functions (In call order) =====

    private static void Set_up_files(){
        try {
            file_todo = new File("todo.txt");
            file_completed = new File("completed.txt");
            file_history = new File("history.txt");
            file_date = new File("date.txt");
            file_show = new File("show.txt");
            file_study = new File("study.txt");
            file_network = new File("network.txt");
            file_removed = new File("removed.txt");
        }
        catch (Exception e) {
            System.err.println("Error assigning file: " + e.getMessage());
        }
    }

    private static void Create_todo_arraylist_from_file(){
        try {
            scanner = new Scanner(file_todo);
        }
        catch(Exception e){
	        System.err.println("Error assigning file: " + e.getMessage());
        }
        String s, section;
        int int_i=0;
        for(int i=0; i<array_section_strings.length && scanner.hasNextLine(); i++){
            array_indexes[i] = int_i;
            section = array_section_strings[i];
            scanner.nextLine();
            while(scanner.hasNextLine()){
                s = scanner.nextLine();
                if(!s.equals("")){
                    arraylist_todo.add(s);
                    int_i++;
                } else {
                    break;
                }
            }
        }
    }

    private static ArrayList<String> Create_arraylist_from_file(File file){
        try {
            scanner = new Scanner(file);
        }
        catch(Exception e){
	        System.err.println("Error assigning file: " + e.getMessage());
        }
        ArrayList<String> list = new ArrayList<String>();
        while(scanner.hasNextLine()){
            list.add(scanner.nextLine());
        }
        return list;
    }

    private static String Get_string_from_file(File file) {
        try {
            scanner = new Scanner(file);
        } catch (Exception e) {
	        System.err.println("Error assigning file: " + e.getMessage());
        }
        return scanner.nextLine();
    }

    private static void Check_date() {
        string_date_on_file = Get_string_from_file(file_date);
        
        if(!string_date_on_file.equals(string_date_today)){
            
            String[] dailies = {
                "exercise",
                "take vitamin D",
                "check email",
                "floss",
                "shave",
            };
            int_completed = 0;
            arraylist_history.add("");
            arraylist_history.add("===== "+string_date_today+" =====");
            String s;
            for(int i = dailies.length-1; i>=0; i--){
                s = dailies[i];
                if(!arraylist_todo.contains(s)){
                    Top(s);
                }
            }
            String today = Get_day();
            if(today.equalsIgnoreCase("sun")){
                Add("later", "take recyclables out");
            }

            while(array_indexes[2]!=array_indexes[3]){
                Move(array_indexes[2], "later");
            }
        }
    }

    private static void Print(){
        int seperator = 1;
        p("Number of tasks completed today: "+int_completed);        
        for(int i=0; i<seperator; i++){
            if(array_indexes[i] != array_indexes[i+1]){
                Print_section(array_section_strings[i], array_indexes[i], array_indexes[i+1]);
            }
        }
        for(int i=seperator; i<array_indexes.length-1; i++){
            if(array_indexes[i] != array_indexes[i+1] && bool_show){
                Print_section(array_section_strings[i], array_indexes[i], array_indexes[i+1]);
            }
        }
        if(array_indexes[array_indexes.length-1] != arraylist_todo.size() && bool_show){
            Print_section(array_section_strings[array_indexes.length-1], array_indexes[array_indexes.length-1], arraylist_todo.size());
        }
    }

    private static void User_input() {
        String command = input("Please input a command.");
        if(command.equals("")){
            return;
        }
        String[] command_split = command.split(" ");
        String first_word = command_split[0];
        int index;
        switch(first_word){
            case "do":
            case "add":
                which_section = command_split[1]; // section second
                int start = 1;
                if(Check_section(which_section)){
                    start++;
                }
                Add(which_section, Rebuild_string(command_split, start, command_split.length-1)); // section second
                break;


            case "reset":
                if(command_split[1].equals("history")){
                    arraylist_history.clear();
                } else if(command_split[1].equals("number")){
                    int_completed = 0;
                }
                break;


            case "hide":
                bool_show = false;
                break;


            case "show":
                bool_show = true;
                break;


            case "a":
                Add_studies(arraylist_study, file_study, 1); // section second
                break;


            case "network":
                Add_studies(arraylist_network, file_network, 1); // section second
                break;
            
            
            case "rm":
            case "remove":
                try {
                    index = Integer.parseInt(command_split[1]);
                    Remove(index);
                }
                catch (Exception e) {
	                System.err.println("Error parsing string: " + e.getMessage());
                }
                break;


            case "did":
                try {
                    index = Integer.parseInt(command_split[1]);
                    Did(index);
                }
                catch (Exception e) {
	                System.err.println("Error parsing string: " + e.getMessage());
                }
                break;


            case "top":
                try {
                    index = Integer.parseInt(command_split[1]);
                    Top(index);
                }
                catch (Exception e) {
	                System.err.println("Error parsing string: " + e.getMessage());
                }
                break;


            case "mv":
            case "move":
                try {
                    index = Integer.parseInt(command_split[1]);
                    which_section = command_split[2];
                    Move(index, which_section);
                }
                catch (Exception e) {
	                System.err.println("Error parsing string: " + e.getMessage());
                }
                break;


            case "put":
            case "pt":
                try {
                    index = Integer.parseInt(command_split[1]);
                    int to = Integer.parseInt(command_split[2]);
                    Move(index, to);
                }
                catch (Exception e) {
	                System.err.println("Error parsing string: " + e.getMessage());
                }
                break;


            case "edit":
                try {
                    index = Integer.parseInt(command_split[1]);
                    arraylist_todo.set(index, Rebuild_string(command_split, 2, command_split.length-1));
                }
                catch (Exception e) {
	                System.err.println("Error parsing string: " + e.getMessage());
                }
                break;


            case "insert":
            case "in":
                try {
                    index = Integer.parseInt(command_split[1]);
                    Add(index, Rebuild_string(command_split, 2, command_split.length-1)); // section second
                }
                catch (Exception e) {
	                System.err.println("Error parsing string: " + e.getMessage());
                }
                break;


            default:
                p("You entered an invalid command.");
        }
    }

    private static void Write_todo_file() {
        try {
            writer = new PrintWriter(new FileOutputStream(file_todo), true);
        } catch (Exception e) {
	        System.err.println("Error assigning file: " + e.getMessage());
        }
        for(int i=0; i<array_indexes.length-1; i++){
            Write_section(array_section_strings[i], array_indexes[i], array_indexes[i+1]);
        }
        Write_section(array_section_strings[array_indexes.length-1], array_indexes[array_indexes.length-1], arraylist_todo.size());
        writer.close();
    }

    private static void Write_to_file(Object x, File file) {
        try {
            writer = new PrintWriter(new FileOutputStream(file), true);
        } catch (Exception e) {
        	System.err.println("Error assigning file: " + e.getMessage());
        }
        writer.print(x);
        writer.close();
    }

    private static void Write_to_file(ArrayList<String> arraylist, File file) {
        try {
            writer = new PrintWriter(new FileOutputStream(file), true);
        } catch (Exception e) {
        	System.err.println("Error assigning file: " + e.getMessage());
        }
        for(String s : arraylist){
            writer.print(s+"\n");
        }
        writer.close();
    }

   
    
    
    
    // ===== Sub-Functions (In Alphabetical Order)=====

    private static void Add(String section, String entry){
        if(!arraylist_todo.contains(entry)){
            Shift_sections(section);
            arraylist_todo.add(array_indexes[Convert_section_to_index_in_array_indexes(section)], entry);
        }
    }

    private static void Add(int index, String entry){
        if(!arraylist_todo.contains(entry)){
            Shift_sections(Find_section(index));
            arraylist_todo.add(index, entry);
        }
    }

    private static void Add_removed(String s){
        arraylist_removed.add(s);
        if(arraylist_removed.size()>10){
            arraylist_removed.remove(0);
        }
    }

    private static void Add_studies(ArrayList<String> arraylist, File file, int n) {
        arraylist = Create_arraylist_from_file(file);
        String quiz;
        for(int i = 0; i < n; i++){
            quiz = arraylist.remove(0);
            Top(quiz);
            arraylist.add(quiz);
        }
        Write_to_file(arraylist, file);
    }

    private static boolean Check_section(String section){
        for(int i=0; i<array_section_strings.length; i++){
            if(section.equalsIgnoreCase(array_section_strings[i])){
                return true;
            }
        }
        return false;
    }

    private static int Convert_section_to_index_in_array_indexes(String section){
        for(int i=0; i<array_section_strings.length; i++){
            if(section.equalsIgnoreCase(array_section_strings[i])){
                return i;
            }
        }
        return 0;
    }

    private static void Did(int index){
        arraylist_history.add(Remove(index));
        int_completed++;
    }

    private static String Find_section(int index){
        int i = 0;
        while(i < array_indexes.length && index < array_indexes[i]){
            i++;
        }
        return array_section_strings[i];
    }

    private static String Get_day(){
        LocalDateTime myDateObj = LocalDateTime.now();    
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("E");  
        String formattedDate = myDateObj.format(myFormatObj);  
        return formattedDate;
    }

    private static String input(String s){
        scanner = new Scanner(System.in);
        p(s);
        return scanner.nextLine();
    }

    private static void Move(int index, String section){
        String s = Remove(index);
        Add(section, s);
    }

    private static void Move(int index, int to){
        String s = Remove(index);
        Add(to, s);
    }

    private static void p(Object s){
        System.out.println(s);
    }

    private static void Print_array(int[] array){
        for(Integer s : array){
            System.out.print(s+" ");
        }
        p("");
    }

    private static void Print_arraylist(ArrayList arraylist){
        for(int i=0; i<arraylist.size(); i++){
            p(i+". "+arraylist.get(i));
        }
    }

    private static void Print_section(String section, int start, int end){
        p("===== "+section+" =====");
        for(int i=start; i<end; i++){
            p(i+". "+arraylist_todo.get(i));
        }
        p("");
    }

    private static String Rebuild_string(String[] array, int first, int last){
        String s = array[first];
        for(int i=first+1; i<=last; i++){
            s += " "+array[i];
        }
        return s;
    }

    private static String Remove(int index){
        int marker = 0;
        while(marker<array_indexes.length && index >= array_indexes[marker]){
            marker++;
        }
        for(int i = marker; i<array_indexes.length; i++){
            array_indexes[i]--;
        }
        String s = arraylist_todo.remove(index);
        Add_removed(s);
        return s;
    }

    private static void Shift_sections(String section){
        int int_section = Convert_section_to_index_in_array_indexes(section);
        for(int i=int_section+1; i<array_indexes.length; i++){
            array_indexes[i]++;
        }
    }

    private static void Top(int index){
        Add("now", Remove(index));
    }

    private static void Top(String s){
        Add("now", s);
    }

    private static void Write_section(String section, int start, int end){
        writer.println("===== "+section+" =====");
        for(int i=start; i<end; i++){
            writer.println(arraylist_todo.get(i));
        }
        writer.println("");
    }
}