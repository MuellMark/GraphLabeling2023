import java.util.ArrayList;
import java.util.*;
import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;

// This is the optimized version of the code using arraylists. This is lacking some features from the
// Array based version, and it is a bit slower, but this also functions
class generateGraphsOptimizationTest{
    public static void main(String[]args){
        int numVertices =7; // Change for # of Vertices

        // Stores all possible combinations for all recurssive calls
        ArrayList<edgeStorage> allCombos = new ArrayList<>();
        // Stores all ddmLabelings as the recurssive method runs through
        ArrayList<edgeStorage> ddmLabelings = new ArrayList<>();

        // StartGraph is all zeroes
        edgeStorage startGraph = new edgeStorage(numVertices);
        allCombos.add(startGraph);

        generate(allCombos, numVertices,ddmLabelings); // Starts generating graphs

        // Prints all graphs found, then writes to files
        // Several different version, change depending on desired output
        printAllAdjMatrix(ddmLabelings);
        //writeAllCombosToFileVisualization(ddmLabelings);
        //writeAllCombosToFileAdjMatrix(ddmLabelings);
        // For debugging optimization test
        //writeAllCombosToFile(allCombos);
    }

    // Loops through all recursive calls from checkforSums
    public static void generate(ArrayList<edgeStorage> allCombos, int vertex,ArrayList<edgeStorage> ddmLabelings){
        // Loops through all vertices in reverse order
        while(vertex>0){
            ArrayList<Integer> set = getUsuableSet(vertex); // Gets usable edge set
            int currentSize=allCombos.size(); // Makes sure not an infinite loop, only checks current graphs
            System.out.println("Graphs generated so far: "+currentSize);
            // Does a recurrsive call to all graphs in allCombos
            for(int i=0;i<currentSize;i++){
                edgeStorage temp = allCombos.get(i).copy(); // Creates copy to avoid pointer issues
                checkForSums(allCombos, temp, set, vertex, 0,ddmLabelings); // Recursive call
            }
            vertex--;
        }
    }

    // Recursive method, finds all possible combinations of edges TODO: potentailly add a max value to check
    public static void checkForSums(ArrayList<edgeStorage> allCombos, edgeStorage current,
    ArrayList<Integer> set, int vertex, int index, ArrayList<edgeStorage> ddmLabelings){
        int sumIns = current.getSumIns(vertex);
        int sumOuts = current.getSumOuts(vertex);

        // if(sumIns>7 || sumOuts>7){
        //     System.out.println("Sum ins:"+sumIns+" and Sum Outs:"+sumOuts);
        // }
        // base case, if the two are equal then a potential labeling could be found
        if(sumIns==sumOuts && sumIns>0){
            allCombos.add(current); // Adds to allCombos for future graphs to check from
            if(current.isDDMLabeling()){
                boolean isInvOrEq = false;
                for(int i=0;i<ddmLabelings.size();i++){
                    if(ddmLabelings.get(i).isInverse(current) || ddmLabelings.get(i).equals(current)){
                        isInvOrEq=true;
                        i+=allCombos.size();
                    }
                }
                // If the graph is a valid labeling, is not a repear or inverse, then it's added to ddmLabelings
                if(!isInvOrEq){
                    ddmLabelings.add(current);
                }
            }
        // Otherwise, does a recurrsive case
        }else if(index<set.size() ){
            // Creates copy and adds index to ins
            edgeStorage addToIns = current.copy();
            addToIns.addPair(set.get(index),vertex);
 
            // Creates copy and adds index to outs
            edgeStorage addToOuts = current.copy();
            addToOuts.addPair(vertex,set.get(index));
                
            // Creates copy, does not add index to either ins or outs
            edgeStorage copyCurrent = current.copy(); 
            index++; // Incremenets index

            // Debugging statement, to track number of graphs generated when dealing with large cases
            if(allCombos.size()%10000==0) System.out.println("Graph added, "+allCombos.size()+" graphs found");

            //Recursive calls, one for each new graph created
            checkForSums(allCombos,copyCurrent,set,vertex,index,ddmLabelings);

            checkForSums(allCombos,addToOuts,set,vertex,index,ddmLabelings);

            checkForSums(allCombos,addToIns,set,vertex,index,ddmLabelings);

        }
    }
    //Gets usable set for a vertex, basically all vertices less than the input
    public static ArrayList<Integer> getUsuableSet(int vertex){
        ArrayList<Integer> set = new ArrayList<>();
        for(int i=1;i<vertex;i++){
            set.add(i);
        }
        return set;
    }

    // Prints arraylist of edgeStorage in a readable fassion
    public static void printAllCombos(ArrayList<edgeStorage> AllCombos){
        for(int i=0;i<AllCombos.size();i++){
            System.out.println("Graph #"+(i+1)+":");
            AllCombos.get(i).print();
        }
    }

        // Prints arraylist of edgeStorage in an adjacency matrix for easy checking
        public static void printAllAdjMatrix(ArrayList<edgeStorage> AllCombos){
            for(int i=0;i<AllCombos.size();i++){
                System.out.println("Graph #"+(i+1)+":");
                AllCombos.get(i).printAdjMatrix();
            }
        }

    // LEGACY, keeping in case needed in future 
    // Filters results, stores all non-repeated DDM labelings in a new Arraylist, returns that
    public static ArrayList<edgeStorage> filterResults(ArrayList<edgeStorage> allCombos){
        ArrayList<edgeStorage> filtered = new ArrayList<>(); // Stores the filtered results
        for(int i=0;i<allCombos.size();i++){
            if(allCombos.get(i).isDDMLabeling()){ //Checks if it is a DDM
                boolean isRepeat= false; // Checks if repeat
                for(int j=0;j<filtered.size();j++){
                    if(filtered.get(j).equals(allCombos.get(i))) isRepeat=true;
                }
                if(!isRepeat) filtered.add(allCombos.get(i));
            }
        }
        return filtered;
    }

    // LEGACY, keeping in case needed in future 
    // If a graph is just the inverse of another graph, not kept, to be used in addition to 
    // filterResults, not instead of it
    public static ArrayList<edgeStorage> filterInverseResults(ArrayList<edgeStorage> allCombos){
        ArrayList<edgeStorage> filtered = new ArrayList<>(); // Stores the filtered results
        for(int i=0;i<allCombos.size();i++){
            boolean isRepeat= false; // Checks if inverse
            for(int j=0;j<filtered.size();j++){
                if(filtered.get(j).isInverse(allCombos.get(i))) isRepeat=true;
            }
            if(!isRepeat) filtered.add(allCombos.get(i));
        }
        return filtered;
    }

    // calls sort on every graph in allCombos, sorting the ins and outs for easier checking
    public static void sortAllCombos(ArrayList<edgeStorage> allCombos){
        for(int i=0;i<allCombos.size();i++){
            allCombos.get(i).sort();
        }
    }

    // Writes the arraylist of combinations to a file in a readbale way
    public static void writeAllCombosToFile(ArrayList<edgeStorage> AllCombos){
        try {
            // Opens files
            FileWriter myWriter = new FileWriter("/Users/markymarkscomputer/Desktop/Untitled/output.txt");
            for(int i=0;i<AllCombos.size();i++){
                myWriter.write("\nGraph #"+(i+1)+":\n");
                AllCombos.get(i).writeToFile(myWriter);
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
 
    }

    // Writes to a file in an adjacency matrix for manual checking
    public static void writeAllCombosToFileAdjMatrix(ArrayList<edgeStorage> AllCombos){
        try {
            // Opens file
            FileWriter myWriter = new FileWriter("/Users/markymarkscomputer/Desktop/Untitled/output.txt");
            for(int i=0;i<AllCombos.size();i++){
                myWriter.write("\nGraph #"+(i+1)+":\n");
                AllCombos.get(i).writeToFileAdjMat(myWriter);
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Writes to file to be read in by visualizeGraphs.py
    public static void writeAllCombosToFileVisualization(ArrayList<edgeStorage> AllCombos){
        try {
            // Opens File
            FileWriter myWriter = new FileWriter("/Users/markymarkscomputer/Desktop/Untitled/outputForVis.txt");
            for(int i=0;i<AllCombos.size();i++){
                AllCombos.get(i).writeToFileForVisualization(myWriter);
                myWriter.write("\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}