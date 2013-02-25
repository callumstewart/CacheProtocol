/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cacheprotocol;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 *
 * @author callumstewart
 */
public class ProcessorCacheTest {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

	int cacheLines = Integer.parseInt(args[0]);
	int lineSize = Integer.parseInt(args[1]);
	int enablePrinting = Integer.parseInt(args[2]);
	int noOfProcessors = Integer.parseInt(args[3]);
	String tracefile = args[4];
	ProcessorCache[] ListOfSeparateProcesses = new ProcessorCache[noOfProcessors];
	int[][][] accessToCacheLine = new int[4][cacheLines][34];
	int[][][] accessToCacheLineRead = new int[4][cacheLines][34];
	int[][][] accessToCacheLineWrite = new int[4][cacheLines][34];


	for (int i = 0; i < ListOfSeparateProcesses.length; i++) {
	    ListOfSeparateProcesses[i] = new ProcessorCache(cacheLines, lineSize, i, enablePrinting, noOfProcessors);
	}


	for (int i = 0; i < ListOfSeparateProcesses.length; i++) {
	    ListOfSeparateProcesses[i].makeAquaintance(ListOfSeparateProcesses);
	}




	String processor;
	int address;
	String operation;

	try {
	    FileInputStream fstream = new FileInputStream(tracefile);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    while ((strLine = br.readLine()) != null) {
		String[] tokens = strLine.split(" ");
		processor = tokens[0];
		operation = tokens[1];
		address = Integer.parseInt(tokens[2]);
		int op;
		int processSelect = Integer.parseInt(processor.substring(1));


		if (operation.matches("R")) {
		    op = 0;
		    ListOfSeparateProcesses[processSelect].ProcessOperation(address, op);
		} else {
		    op = 1;
		    ListOfSeparateProcesses[processSelect].ProcessOperation(address, op);
		}






	    }
	    in.close();
	} catch (Exception e) {
	    System.err.println("Error: " + e.getMessage());
	}




	for (int i = 0; i < ListOfSeparateProcesses.length; i++) {
	    System.out.println("============================================================================================");
	    System.out.println("Process " + i + " Read Hits : " + ListOfSeparateProcesses[i].getReadHits());
	    System.out.println("Process " + i + " Read Misses : " + ListOfSeparateProcesses[i].getReadMisses());
	    System.out.println("Process " + i + " Read Hit Rate : " + ListOfSeparateProcesses[i].getReadHitRate() + "%");
	    System.out.println("Process " + i + " Read Miss Rate : " + ListOfSeparateProcesses[i].getReadMissRate() + "%");
	    System.out.println("Process " + i + " Write Hits : " + ListOfSeparateProcesses[i].getWriteHits());
	    System.out.println("Process " + i + " Write Misses : " + ListOfSeparateProcesses[i].getWriteMisses());
	    System.out.println("Process " + i + " Write Hit Rate : " + ListOfSeparateProcesses[i].getWriteHitRate() + "%");
	    System.out.println("Process " + i + " Write Miss Rate : " + ListOfSeparateProcesses[i].getWriteMissRate() + "%");
	    System.out.println("Process " + i + " Invalidates : " + ListOfSeparateProcesses[i].getInvalidates());
	    System.out.println("Process " + i + " Read Operations : " + ListOfSeparateProcesses[i].getReadOperations());
	    System.out.println("Process " + i + " Write Operations : " + ListOfSeparateProcesses[i].getWriteOperations());
	    System.out.println("Process " + i + " Combined Hit Rate : " + ListOfSeparateProcesses[i].getCombinedHitRate());
	    System.out.println("Process " + i + " Combined Miss Rate : " + ListOfSeparateProcesses[i].getCombinedMissRate());
	    System.out.println("Process " + i + " Read Coherence Misses : " + ListOfSeparateProcesses[i].getreadCoherenceMiss());
	    System.out.println("Process " + i + " Read Coherence Miss Rate : " + ListOfSeparateProcesses[i].getReadCoherenceMissRate());
	    System.out.println("Process " + i + " Write Coherence Misses : " + ListOfSeparateProcesses[i].getwriteCoherenceMiss());
	    System.out.println("Process " + i + " Write Coherence Miss Rate : " + ListOfSeparateProcesses[i].getWriteCoherenceMissRate());
	    System.out.println("Process " + i + " Combined Coherence Miss Rate : " + ListOfSeparateProcesses[i].getCombinedCoherenceMissRate());
	    System.out.println("Process " + i + " Read Bus Action : " + ListOfSeparateProcesses[i].getreadBusAction());
	    System.out.println("Process " + i + " Write Bus Action : " + ListOfSeparateProcesses[i].getwriteBusAction());
	    System.out.println("============================================================================================");

	}

	for (int i = 0; i < ListOfSeparateProcesses.length; i++) {
	    for (int j = 0; j < ListOfSeparateProcesses[i].getTagListTotal().size(); j++) {
		/*

		System.out.printf("Process %d Access Block %d with Tag %d\n", ListOfSeparateProcesses[i].getTagListTotal().get(j).get(2),
			ListOfSeparateProcesses[i].getTagListTotal().get(j).get(0),
			ListOfSeparateProcesses[i].getTagListTotal().get(j).get(1));
		 * 
		 */


		accessToCacheLine[i][ListOfSeparateProcesses[i].getTagListTotal().get(j).get(0)][ListOfSeparateProcesses[i].getTagListTotal().get(j).get(1)]++;

	    }
	    for (int r = 0; r < ListOfSeparateProcesses[i].getTagListRead().size(); r++) {

		accessToCacheLineRead[i][ListOfSeparateProcesses[i].getTagListRead().get(r).get(0)][ListOfSeparateProcesses[i].getTagListRead().get(r).get(1)]++;

	    }
	    for (int w = 0; w < ListOfSeparateProcesses[i].getTagListWrite().size(); w++) {

		accessToCacheLineWrite[i][ListOfSeparateProcesses[i].getTagListWrite().get(w).get(0)][ListOfSeparateProcesses[i].getTagListWrite().get(w).get(1)]++;
	    }
	}

	//Determine Unique Reads.
	int readCount = 0;
	int writeCount = 0;
	int readWriteCount = 0;
	int readTotalShared = 0;
	int readTotalUnique = 0;
	int writeTotalShared = 0;
	int writeTotalUnique = 0;
	int readWriteTotalShared = 0;
	int readWriteTotalUnique =0;
	int readProcessCount = 0;
	int readTotesCount = 0;
	int writeProcessCount = 0;
	int writeTotesCount = 0;
	int readWriteProcessCount = 0;
	int readWriteTotesCount = 0;
	
	int totalCount = 0;
	int oneProcessor = 0;
	int twoProcessor = 0;
	int threeProcessor = 0;
	int fourProcessor = 0;
	
	int totalWriteCoherenceMisses = 0;
	int totalReadCoherenceMisses = 0;
	
	int totalWriteMisses = 0;
	int totalReadMisses = 0;
	
	int totalReadOperations = 0;
	int totalWriteOperations = 0;
	
	for (int i = 0; i < ListOfSeparateProcesses.length; i++) {
	    
	totalWriteCoherenceMisses += ListOfSeparateProcesses[i].getwriteCoherenceMiss();
	totalReadCoherenceMisses += ListOfSeparateProcesses[i].getreadCoherenceMiss();
	
	totalWriteMisses += ListOfSeparateProcesses[i].getWriteMisses();
	totalReadMisses += ListOfSeparateProcesses[i].getReadMisses();
	
	
	totalReadOperations += ListOfSeparateProcesses[i].getReadOperations();
	totalWriteOperations += ListOfSeparateProcesses[i].getWriteOperations();
	}
	
	
	
	int totalReadOnlyOperations = 0;
	int totalOperations = 0;
	int totalWriteOnlyOperations = 0;
	
	int totalMemoryAddressesAccessed = 0;

	for (int k = 0; k < cacheLines; k++) {
	    for (int t = 0; t < 34; t++) {
		for (int i = 0; i < ListOfSeparateProcesses.length; i++) {
		    
		    if (accessToCacheLineRead[i][k][t] != 0 || accessToCacheLineWrite[i][k][t] !=0) {
			totalOperations += accessToCacheLineRead[i][k][t] + accessToCacheLineWrite[i][k][t];
			readWriteCount++;
			if(readWriteCount == 1){
			readWriteProcessCount = accessToCacheLineRead[i][k][t] + accessToCacheLineWrite[i][k][t] ;
			}
			readWriteTotesCount += accessToCacheLineRead[i][k][t] + accessToCacheLineWrite[i][k][t];
		    }
		    
		    if (accessToCacheLineRead[i][k][t] != 0 && accessToCacheLineWrite[0][k][t] == 0 && accessToCacheLineWrite[1][k][t] == 0 
			    && accessToCacheLineWrite[2][k][t] == 0 && accessToCacheLineWrite[3][k][t] == 0) {
			readCount++;
			totalReadOnlyOperations+= accessToCacheLineRead[i][k][t];
			if(readCount == 1){
			readProcessCount = accessToCacheLineRead[i][k][t];
			}
			readTotesCount += accessToCacheLineRead[i][k][t];
		    }
		    
		    if (accessToCacheLineWrite[i][k][t] != 0 && accessToCacheLineRead[0][k][t] == 0 && accessToCacheLineRead[1][k][t] == 0 
			    && accessToCacheLineRead[2][k][t] == 0 && accessToCacheLineRead[3][k][t] == 0) {
			writeCount++;
			totalWriteOnlyOperations+= accessToCacheLineWrite[i][k][t];
			if(writeCount == 1){
			writeProcessCount = accessToCacheLineWrite[i][k][t];
			}
			writeTotesCount += accessToCacheLineWrite[i][k][t];
		    }

		    if(accessToCacheLine[i][k][t] != 0){
			totalCount++;
		    }
			
		}
		

		if (readWriteCount == 1) {
		    readWriteTotalUnique+= readWriteProcessCount;
		   
		} else if (readWriteCount > 1) {
		    readWriteTotalShared += readWriteTotesCount;
		    
		}
		
		if (readCount == 1) {
		 
		    readTotalUnique+= readProcessCount;
		} else if (readCount > 1) {
		    
		    readTotalShared += readTotesCount;
		}
		
		
		if (writeCount == 1) {
		 
		    writeTotalUnique+= writeProcessCount;
		} else if (writeCount > 1) {
		    
		    writeTotalShared += writeTotesCount;
		}
		
		if (totalCount == 1){
		    oneProcessor++;
		}else if(totalCount == 2){
		    twoProcessor++;
		}else if(totalCount == 3){
		    threeProcessor++;
		}else if(totalCount == 4){
		    fourProcessor++;
		}
		
		readWriteCount = 0;
		readCount = 0;
		writeCount = 0;
		readWriteTotesCount = 0;
		readTotesCount = 0;
		writeTotesCount = 0;
		totalCount = 0;

	    }





	}
	
	//% of memory accesses to private cache lines
	
	double privateCacheLineAccesses = ((double)readWriteTotalUnique/totalOperations) * 100;
	
	//% of memory accesses to shared read only cache lines
	
	double sharedReadOnlyCacheLineAccesses = ((double)readTotalShared/totalOperations)*100;
	
	//% of memory accesses to shared cache lines
	
	double sharedCacheLineAccesses = ((double)readWriteTotalShared/totalOperations)*100;
	
	
	//Total Memory Addresses Accessed
	
	totalMemoryAddressesAccessed = oneProcessor + twoProcessor + threeProcessor + fourProcessor;
	
	//Perntage Accessed by 1 Processor
	
	double oneProcessorAccess = ((double)oneProcessor/totalMemoryAddressesAccessed)*100;
	
	//Percentage Accessed by 2 Processors
	
	double twoProcessorAccess = ((double)twoProcessor/totalMemoryAddressesAccessed)*100;
	
	//Percentage Accessed by more than 2 Processors
	
	double moreThanTwoProcessorAccess = ((double)(threeProcessor + fourProcessor)/totalMemoryAddressesAccessed)*100;
	
	
	//Total Read Miss Rate
	
	double totalReadMissRate = ((double)totalReadMisses/totalReadOperations)*100;

	//Total Write Miss Rate 
	
	double totalWriteMissRate = ((double)totalWriteMisses/totalWriteOperations) * 100;
	
	//Total Miss Rate
	
	double totalMissRate = ((double)(totalWriteMisses + totalReadMisses)/totalOperations)*100;
	
	//Total Write Coherence Miss Rate
	
	double totalWriteCoherenceMissRate = ((double)totalWriteCoherenceMisses/totalWriteMisses*100);
	
	//Total Read Coherence Miss Rate
	
	double totalReadCoherenceMissRate = ((double)totalReadCoherenceMisses/totalReadMisses)*100;
	
	//Total Coherence Miss Rate
	
	double totalCoherenceMissRate = ((double)(totalReadCoherenceMisses+totalWriteCoherenceMisses)/(totalReadMisses+totalWriteMisses))*100;

	
	
	
	
	System.out.printf("\nTotal Read Miss Rate: %f",totalReadMissRate);
	System.out.print("%");
	
	System.out.printf("\nTotal Write Miss Rate: %f",totalWriteMissRate);
	System.out.print("%");
	
	System.out.printf("\nTotal Miss Rate: %f",totalMissRate);
	System.out.print("%");
	
	System.out.printf("\nTotal Write Coherence Miss Rate: %f",totalWriteCoherenceMissRate);
	System.out.print("%");
	
	System.out.printf("\nTotal Read Coherence Miss Rate: %f",totalReadCoherenceMissRate);
	System.out.print("%");
	
	System.out.printf("\nTotal Coherence Miss Rate: %f",totalCoherenceMissRate);
	System.out.print("%\n");
	
	
	System.out.printf("\nAccesses to Read Only Private Lines: %d\n", readTotalUnique);
	System.out.printf("Accesses to Read Only Shared Lines: %d\n", readTotalShared);
	System.out.printf("Accesses to Read/Write Private Lines: %d\n", readWriteTotalUnique);
	System.out.printf("Accesses to Read/Write Shared Lines: %d\n", readWriteTotalShared);
	System.out.printf("Accesses to Write Only Private Lines: %d\n", writeTotalUnique);
	System.out.printf("Accesses to Write Only Shared Lines: %d\n\n", writeTotalShared);

	System.out.printf("Memory Addresses Accessed by 1 Processor: %d\n", oneProcessor);
	System.out.printf("Memory Addresses Accessed by 2 Processors: %d\n", twoProcessor);
	System.out.printf("Memory Addresses Accessed by 3 Processors: %d\n", threeProcessor);
	System.out.printf("Memory Addresses Accessed by 4 Processors: %d\n", fourProcessor);
	
	System.out.printf("\nMemory Accesses by 1 Processor: %f",oneProcessorAccess);
	System.out.print("%");
	
	System.out.printf("\nMemory Accesses by 2 Processors: %f",twoProcessorAccess);
	System.out.print("%");
	
	System.out.printf("\nMemory Accesses by more than 2 Processors: %f",moreThanTwoProcessorAccess);
	System.out.print("%\n");
	
	System.out.printf("\nMemory Accesses to Private Cache Lines: %f",privateCacheLineAccesses);
	System.out.print("%");
	
	System.out.printf("\nMemory Accesses to Shared Read Only Cache Lines: %f",sharedReadOnlyCacheLineAccesses);
	System.out.print("%");
	
	System.out.printf("\nMemory Accesses to Shared Cache Lines: %f",sharedCacheLineAccesses);
	System.out.print("%\n");
	

	System.out.printf("\nTotal Read Only Operations: %d\n", totalReadOnlyOperations);
	System.out.printf("Total Write Only Operations: %d\n", totalWriteOnlyOperations);
	System.out.printf("Total Operations: %d\n", totalOperations);
	/*
	 * 
	 * Calculate Percentages
	 * 
	 * Percentage of Read Misses for Each Processor ||
	 * Percentage of Write Misses for Each Processor ||
	 * 
	 * Combined Percentage of Misses for Each Processor ||
	 * 
	 * Percentage of Read Coherence Misses ||
	 * Percentage of Write Coherence Misses ||
	 * 
	 * Combined Percentage of Coherence Misses ||
	 * 
	 * Percentage of Read Only Private Lines
	 * Percentage of Read Only Shared Lines ***
	 * 
	 * Percentage of Private Cache Lines (Read/Write) ***
	 * Percentage of Shared Cache Lines (Read/Write) ***
	 * 
	 * Percentage of Memory Accesses by 1 Process
	 * Percentage of Memory Accesses by 2 Processes
	 * Percentage of Memory Accesses by 3 or More Processes
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	
	if(enablePrinting == 1){
	
	    
	System.out.printf("\n\n Accesses To Blocks in Cache\n\n");
	System.out.printf(" P0 Tot | P0 R | P0 W |  | P1 Tot | P1 R | P1 W |  | P2 Tot | P2 R | P2 W "
		+ "|  | P3 Tot | P3 R | P3 W |  |\n\n");
	for(int j = 0; j< cacheLines; j++){
	
	for(int x = 0; x < ListOfSeparateProcesses.length; x++){
	
	
	System.out.printf(" %-6s",Integer.toString(ListOfSeparateProcesses[x].getPrivateCacheLines(j,0) ),args);
	System.out.print("|");
	System.out.printf(" %-6s",Integer.toString(ListOfSeparateProcesses[x].getPrivateCacheLines(j,1) ),args);
	System.out.print("|");
	System.out.printf(" %-6s",Integer.toString(ListOfSeparateProcesses[x].getPrivateCacheLines(j,2) ),args);
	System.out.print("| |");
	}
	
	System.out.println("");
	}
	}
	
	}

    }

