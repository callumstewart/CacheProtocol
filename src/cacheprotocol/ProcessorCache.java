/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cacheprotocol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author callumstewart
 */
public class ProcessorCache {
    //Input Parameters

    int cacheLines;
    int lineSize;
    int processNumber;
    //States
    static final int MODIFIED = 0;
    static final int SHARED = 1;
    static final int INVALID = 2;
    static final int CACHELAT = 2;
    static final int BUSLAT = 20;
    static final int MEMORYLAT = 200;
    static final int READBYPASS = 1;
    //Cache Addressing Parameters
    int[] cacheTag;
    int[] initialised; //0 false, 1 true
    int[] state; //Modified, Shared or Invalid for each index
    int currentAddress;
    //Parameters to Quantify Data
    private int numberOfReadHits;
    private int readCoherenceMiss;
    private int writeCoherenceMiss;
    private int numberOfReadMisses;
    private int numberOfWriteHits;
    private int numberOfWriteMisses;
    private int invalidations;
    private int numberOfReadOperations;
    private int numberOfWriteOperations;
    private int writeBusAction;
    private int readBusAction;
    private int processorLatency;
    private int bufferSize;
    private boolean TSO = false;
    private int writeLatency;
    private int expectedCompleteTime;
    private boolean newData;
    private int[] currentIndexTag = new int[3];
    /**
     * 
     */
    public int[] privateCacheLines;
    /**
     * 
     */
    public int[] privateReadCacheLines;
    Queue<int[]> storeBuffer;
    /**
     * 
     */
    public int[] privateWriteCacheLines;
    //Boolean Flags used to determine when to Communicate Misses on Communication Bus
    boolean readMissOnBus;
    boolean writeMissOnBus;
    //Debugging Parameter - Gives PrintOut to Console for Each Operation
    int enablePrinting;
    /*Create an array of Processors to enable communication between
    each one in order to emulate bus communication*/
    ProcessorCache[] listOfProcessors;
    ArrayList<ArrayList<Integer>> tagListTotal = new ArrayList<ArrayList<Integer>>();
    ArrayList<ArrayList<Integer>> tagListRead = new ArrayList<ArrayList<Integer>>();
    ArrayList<ArrayList<Integer>> tagListWrite = new ArrayList<ArrayList<Integer>>();
    private int retirementPolicy;

    /**
     * Constructor method used to set up the cache depending on the input parameters
     * 
     * @param cacheLines number of lines in cache
     * @param lineSize   number of words per line
     * @param processNumber unique identifier for each Cache
     * @param enablePrinting  - Integer Boolean used for debugging and Printing to Console
     * @param noOfProcessors  
     */
    public ProcessorCache(int cacheLines, int lineSize, int processNumber, int enablePrinting, int noOfProcessors, int bufferSize, int retirementPolicy) {

	//Populate Data from input
	this.cacheLines = cacheLines;
	this.lineSize = lineSize;
	this.processNumber = processNumber;
	this.enablePrinting = enablePrinting;
	this.processorLatency = 0;
	this.bufferSize = bufferSize;
	this.newData = true;
	this.retirementPolicy = retirementPolicy;

	//Set up Size and initial Values of Processor Cache
	cacheTag = new int[cacheLines];
	initialised = new int[cacheLines];
	state = new int[cacheLines];
	privateCacheLines = new int[cacheLines];
	privateReadCacheLines = new int[cacheLines];
	privateWriteCacheLines = new int[cacheLines];
	listOfProcessors = new ProcessorCache[noOfProcessors];
	storeBuffer = new LinkedList<int[]>();
	//Reset Boolean Bus Flags
	readMissOnBus = false;
	writeMissOnBus = false;


	//reset initialised to zero (empty cache) and all states to invalid
	for (int i = 0; i <= cacheLines - 1; i++) {
	    initialised[i] = 0;
	    state[i] = INVALID;
	    privateCacheLines[i] = 0;
	    privateReadCacheLines[i] = 0;
	    privateWriteCacheLines[i] = 0;
	}
	
	
	
	
    }

    /**
     * Method used as a means for the simulator to communicate with each separate
     * cache Process.  This method takes in the address and operation from the simulator
     * and decides which action (Read or Write) to perform
     * 
     * 
     * @param address the input address from the trace file
     * @param operation whether the trace file specifies a Read (int 0) or Write (int 1)
     */
    public void ProcessOperation(int address, int operation) {
	
	
	boolean add = false;
	//Gives a global reference to current address
	currentAddress = address;
	//The Cache Block (or Index)
	int index = (int) (address / lineSize) % cacheLines;
	//The tag associated with the memory address
	int tag = (int) (address / lineSize) / cacheLines;
	//Basic Case statement determining whether to call read or Write based upon input

	ArrayList<Integer> tagList = new ArrayList<Integer>();
	ArrayList<Integer> tagListReadLocal = new ArrayList<Integer>();
	ArrayList<Integer> tagListWriteLocal = new ArrayList<Integer>();
	privateCacheLines[index]++;
	//tagList.add(address);
	tagList.add(index);
	tagList.add(tag);
	tagList.add(processNumber);
	tagList.add(address);
	tagListTotal.add(tagList);
	
	int[] indextag = new int[3];
	
	indextag[0] = index;
	indextag[1] = tag;
	indextag[2] = address;
	
	
	
	
	
	
	
	
	switch (operation) {
	    case 0:
		tagListReadLocal.add(index);
		tagListReadLocal.add(tag);
		tagListReadLocal.add(processNumber);

		//if (!tagListRead.contains(tagListReadLocal)) {
		tagListRead.add(tagListReadLocal);
		//}
		ProcessRead(index, tag);
		privateReadCacheLines[index]++;
		break;
	    case 1:
		
		tagListWriteLocal.add(index);
		tagListWriteLocal.add(tag);
		tagListWriteLocal.add(processNumber);

		//if (!tagListWrite.contains(tagListWriteLocal)) {
		tagListWrite.add(tagListWriteLocal);
		//}

		/* IMPLEMENTING TSO
		 *
		 * 
		 */
		
		if (TSO) {
		    
		    
		    
		    if (storeBuffer.size() < bufferSize) {
			storeBuffer.add(indextag);
			System.out.printf("This address has been added to the Store Buffer - %d\n\n",address);
			
			
			
			
			
			
		    } else {
			
			System.out.printf("Store Buffer has reached Capacity\n");
			
			storeBuffer.poll();
			
			processorLatency = expectedCompleteTime;
			
			storeBuffer.add(indextag);
			System.out.printf("This address has been added to the Store Buffer - %d\n\n",address);
			
			newData = true;
		    }
		    
		    
		} else {
		    processorLatency += ProcessWrite(index, tag, currentAddress);
		}
		
		
		
		privateWriteCacheLines[index]++;
	}
	
	
	checkWriteBuffer();
	System.out.printf("Current Latency Count %d, Expected Complete Count %d -(Process %d)\n\n", processorLatency, expectedCompleteTime, processNumber);
	System.out.printf("BUFFER SIZE %d\n\n", storeBuffer.size());
    }
    
    private void ProcessRead(int index, int tag) {
	//Count of Read Operations
	numberOfReadOperations++;


	//PrintOut of the Read Access
	if (enablePrinting == 1) {
	    System.out.printf("CACHE READ: Processor %d is looking for Address %d in Cache Block %d with Tag %d. [CURRENT STATE : %d]\n",
		    processNumber, currentAddress, index, tag, state[index]);
	}
	//Cache Block has not been initialised, hence INVALID STATE

	if (foundInQueue(index, tag)) {
	    
	    processorLatency += READBYPASS;
	    
	    if (enablePrinting == 1) {
		System.out.printf("Block %d was found with Tag %d in StoreBuffer (READ BYPASS +1 Latency - Read Hit. Total Read Hits: %d\n",
			index, tag, numberOfReadHits);
	    }
	    
	} else if (initialised[index] == 0) {
	    //Cache is empty hence load index into Cache
	    cacheTag[index] = tag;
	    //Initialise Block
	    initialised[index] = 1;
	    //This is a READ MISS so transition to SHARED state, increment ReadMiss count and set readMissBus Flag
	    state[index] = SHARED;
	    numberOfReadMisses++;
	    readMissOnBus = true;
	    processorLatency += CACHELAT + BUSLAT + MEMORYLAT;
	    //PrintOut of result
	    if (enablePrinting == 1) {
		
		System.out.printf("Block %d has not been initialised - Read Miss. Total Read Misses: %d\n",
			index, numberOfReadMisses);
		
		System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d. [INVALID -> SHARED]\n\n",
			index, processNumber, tag);
		
	    }
	    //Check if the Cache Block mathces the current Address
	} else if (initialised[index] == 1 && cacheTag[index] == tag) {
	    //If there is a READ HIT and state is NOT INVALID
	    if (state[index] != INVALID) {
		//then we can increment ReadHits count, and reset the readMissBus Flag
		numberOfReadHits++;
		readMissOnBus = false;
		processorLatency += CACHELAT;
		//PrintOut of Result
		if (enablePrinting == 1) {
		    
		    System.out.printf("Block %d was found with Tag %d in Cache with a Valid State - Read Hit. Total Read Hits: %d\n",
			    index, tag, numberOfReadHits);
		    
		    System.out.printf("Action: Data is read from Block %d in Process %d's Cache\n\n",
			    index, processNumber);
		    
		}
		//If there is a 'READ HIT' in an INVALID STATE then this is technically a READ MISS 
	    } else {
		//Transition to the SHARED state, increment ReadMiss count and set readMissBus Flag
		state[index] = SHARED;
		numberOfReadMisses++;
		readMissOnBus = true;
		readCoherenceMiss++;
		processorLatency += CACHELAT + BUSLAT + MEMORYLAT;
		//PrintOut of results
		if (enablePrinting == 1) {
		    
		    System.out.printf("Block %d was found with Tag %d in Cache.  Block has been Invalidated by another Process - Read Miss. "
			    + "Total Read Misses: %d\n", index, tag, numberOfReadMisses);
		    
		    System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d. [INVALID -> SHARED]\n\n",
			    index, processNumber, tag);
		}
	    }
	    //Final check is for normal READ MISS without matching tag
	} else if (initialised[index] == 1 && cacheTag[index] != tag) {
	    //This is a readMiss and load in data from memory, increment readMiss count and set readMissBus Flag
	    cacheTag[index] = tag;
	    numberOfReadMisses++;
	    readMissOnBus = true;
	    processorLatency += CACHELAT + BUSLAT + MEMORYLAT;

	    //PrintOut of Results
	    if (enablePrinting == 1) {
		
		if (state[index] == INVALID) {
		    
		    System.out.printf("Block %d did not contain the Tag %d - Read Miss. Total Read Misses: %d\n",
			    index, tag, numberOfReadMisses);
		    
		    System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d. [INVALID -> SHARED]\n\n",
			    index, processNumber, tag);
		    
		} else if (state[index] == SHARED) {
		    
		    System.out.printf("Block %d did not contain the Tag %d - Read Miss. Total Read Misses: %d\n",
			    index, tag, numberOfReadMisses);
		    
		    System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d. [SHARED -> SHARED]\n\n",
			    index, processNumber, tag);
		    
		} else if (state[index] == MODIFIED) {
		    
		    System.out.printf("Block %d did not contain the Tag %d - Read Miss. Total Read Misses: %d\n",
			    index, tag, numberOfReadMisses);
		    
		    System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d. [MODIFIED -> SHARED]\n\n",
			    index, processNumber, tag);
		}
	    }

	    //Transition to SHARED State
	    state[index] = SHARED;
	}
	//Check of readMissBus flag has been set
	if (readMissOnBus) {
	    //If true then call BusReadMiss method for all the other Processors
	    for (int i = 0; i < listOfProcessors.length; i++) {
		if (i != processNumber) {
		    listOfProcessors[i].BusReadMiss(index, tag, processNumber);
		}
	    }
	}
    }
    
    private int ProcessWrite(int index, int tag, int address) {
	currentAddress = address;
	//Increment Operation Count
	numberOfWriteOperations++;
	//PrintOut of Write Access
	if (enablePrinting == 1) {
	    
	    System.out.printf("CACHE WRITE: Processor %d is looking for Address %d in Cache Block %d with Tag %d. [CURRENT STATE : %d]\n",
		    processNumber, currentAddress, index, tag, state[index]);
	}
	//Check if Cache Block has Been Initialised
	if (initialised[index] == 0) {
	    //INVALID STATE, hence transition to MODIFIED state
	    state[index] = MODIFIED;
	    //This is a WRITE MISS, increment writeMiss count and set writeMissBus flag
	    numberOfWriteMisses++;
	    writeMissOnBus = true;
	    writeLatency = CACHELAT + BUSLAT + MEMORYLAT;
	    //load address into cache and then write to it - write-allocate;
	    initialised[index] = 1;
	    cacheTag[index] = tag;
	    //PrintOut of Results
	    if (enablePrinting == 1) {
		
		System.out.printf("Block %d has not been initialised - Write Miss. Total Write Misses: %d\n",
			index, numberOfWriteMisses);
		
		System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d and contents written to it. [INVALID -> MODIFIED]\n\n",
			index, processNumber, tag);
	    }
	    //Check to see if Address Tag is currently stored in Cache
	} else if (initialised[index] == 1 && cacheTag[index] == tag) {
	    // We have a WRITE HIT - but only if in MODIFIED State
	    if (state[index] == MODIFIED) {
		//WRITE HIT, increment WriteHit count and reset WriteMissBus flag (State remains MODIFIED)
		numberOfWriteHits++;
		writeMissOnBus = false;
		writeLatency = CACHELAT;
		//PrintOut of Results
		if (enablePrinting == 1) {
		    
		    System.out.printf("Block %d was in Cache in MODIFIED state - Write Hit. Total Write Hits: %d\n",
			    index, numberOfWriteHits);
		    
		    System.out.printf("Action: New Data is written to Block %d in Process %d's Cache\n\n",
			    index, processNumber);
		}
		//If the current state is NOT MODIFIED
	    } else if (state[index] != MODIFIED) {
		//We Have a WRITE MISS, as either the current Block is INVALID, or SHARED with another Process
		//Increment WriteMiss count,set writeMissBus Flag and transition to MODIFIED State
		numberOfWriteMisses++;
		writeMissOnBus = true;
		writeCoherenceMiss++;
		writeLatency = CACHELAT + BUSLAT + MEMORYLAT;
		//PrintOut of Results
		if (enablePrinting == 1) {
		    if (state[index] == INVALID) {
			
			
			System.out.printf("Block %d was found with Tag %d in Cache.  Block has been Invalidated by another Process - Write Miss. Total Write Misses: %d\n",
				index, tag, numberOfWriteMisses);
			
			System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d and contents written to it. [INVALID -> MODIFIED]\n\n",
				index, processNumber, tag);
			
		    } else if (state[index] == SHARED) {
			
			System.out.printf("Block %d with Tag %d was found in Cache. State was SHARED - Write Miss. Total Write Misses: %d\n",
				index, tag, numberOfWriteMisses);
			
			System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d and contents written to it. [SHARED -> MODIFIED]\n\n",
				index, processNumber, tag);
			
		    }
		}
		state[index] = MODIFIED;
		
	    }
	    //Final Check - Normal WRITE MISS
	} else if (initialised[index] == 1 && cacheTag[index] != tag) {
	    //Load Data into Cache before Writing
	    cacheTag[index] = tag;
	    //Transition to MODIFIED State, increment writeMissCount and set writeMissBus flag

	    numberOfWriteMisses++;
	    writeMissOnBus = true;
	    writeLatency = CACHELAT + BUSLAT + MEMORYLAT;
	    //PrintOut of Results
	    if (enablePrinting == 1) {
		
		if (state[index] == INVALID) {
		    
		    System.out.printf("Block %d did not contain the Tag %d - Write Miss. Total Write Misses: %d\n",
			    index, tag, numberOfWriteMisses);
		    
		    System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d and contents written to it. [INVALID -> MODIFIED]\n\n",
			    index, processNumber, tag);
		    
		} else if (state[index] == SHARED) {
		    
		    System.out.printf("Block %d did not contain the Tag %d - Write Miss. Total Write Misses: %d\n",
			    index, tag, numberOfWriteMisses);
		    
		    System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d and contents written to it. [SHARED -> MODIFIED]\n\n",
			    index, processNumber, tag);
		    
		} else if (state[index] == MODIFIED) {
		    
		    System.out.printf("Block %d did not contain the Tag %d - Write Miss. Total Write Misses: %d\n",
			    index, tag, numberOfWriteMisses);
		    
		    System.out.printf("Action: Block %d for Process %d has been loaded with Tag %d and contents written to it. [MODIFIED -> MODIFIED]\n\n",
			    index, processNumber, tag);
		}
	    }
	    state[index] = MODIFIED;
	    
	}
	//Check if writeMissBus flag is set
	if (writeMissOnBus) {
	    //If true then call BusWriteMiss method for all the other Processors
	    for (int i = 0; i < listOfProcessors.length; i++) {
		if (i != processNumber) {
		    listOfProcessors[i].BusWriteMiss(index, tag, processNumber);
		}
	    }
	}
	
	return writeLatency;
    }

    /**
     * 
     * @param index calculated index number in cache
     * @param tag tag value associated with address
     * @param PN  
     */
    public void BusReadMiss(int index, int tag, int PN) {
	//Check if this process has the index stored in cache
	if (cacheTag[index] == tag) {

	    //If the Current State is MODIFIED - transition to SHARED State
	    if (state[index] == MODIFIED) {
		state[index] = SHARED;
		readBusAction++;
		//PrintOut of Results
		if (enablePrinting == 1) {
		    
		    System.out.printf("Bus Read Miss (Read Miss on Process %d): Process %d changed Block %d with Tag %d from MODIFIED to SHARED\n\n",
			    PN, processNumber, index, tag);
		}
	    }
	}
    }

    /**
     * 
    @param index calculated index number in cache
     * @param tag tag value associated with address
     * @param PN  
     */
    public void BusWriteMiss(int index, int tag, int PN) {
	//Check if this process has the index stored in cache
	if (cacheTag[index] == tag) {

	    //If the Current State is NOT INVALID - transition to INVALID STATE
	    if (state[index] != INVALID) {
		state[index] = INVALID;
		writeBusAction++;
		//Increment Invalidations count
		invalidations++;
		//PrintOut of Results
		if (enablePrinting == 1) {
		    
		    System.out.printf("Bus Write Miss (Write Miss on Process %d): Process %d has Invalidated Block %d with Tag %d\n\n",
			    PN, processNumber, index, tag);
		}
	    }
	}
	
    }

    /**
     * 
     * @return number of invalidations for this Process
     */
    public int getInvalidates() {
	
	return invalidations;
    }

    /**
     * 
     * @return number of Read Hits for this Process
     */
    public int getReadHits() {
	
	return numberOfReadHits;
    }

    /**
     * 
     * @return number of Read Misses for this Process
     */
    public int getReadMisses() {
	
	return numberOfReadMisses;
    }

    /**
     * 
     * @return number of Write Hits for this Process
     */
    public int getWriteHits() {
	
	return numberOfWriteHits;
    }

    /**
     * 
     * @return total number of Read Operations for this Process
     */
    public int getReadOperations() {
	
	return numberOfReadOperations;
    }

    /**
     * 
     * @return total number of Write Operations for this Process
     */
    public int getWriteOperations() {
	
	return numberOfWriteOperations;
    }

    /**
     * 
     * @return number of Write Misses for this Process
     */
    public int getWriteMisses() {
	
	return numberOfWriteMisses;
	
    }

    /**
     * 
     * @return
     */
    public int getreadCoherenceMiss() {
	
	return readCoherenceMiss;
	
    }

    /**
     * 
     * @return
     */
    public int getwriteCoherenceMiss() {
	
	return writeCoherenceMiss;
	
    }

    /**
     * 
     * @return
     */
    public int getwriteBusAction() {
	
	return writeBusAction;
	
    }

    /**
     * 
     * @return
     */
    public int getreadBusAction() {
	
	return readBusAction;
	
    }

    /**
     * 
     * @return
     */
    public ArrayList<ArrayList<Integer>> getTagListTotal() {
	
	return tagListTotal;
	
    }

    /**
     * 
     * @return
     */
    public ArrayList<ArrayList<Integer>> getTagListRead() {
	
	return tagListRead;
	
    }

    /**
     * 
     * @return
     */
    public ArrayList<ArrayList<Integer>> getTagListWrite() {
	
	return tagListWrite;
	
    }

    /**
     * 
     * @return calculated Read Hit Rate Percentage
     */
    public double getReadHitRate() {
	
	return ((double) numberOfReadHits / ((double) numberOfReadOperations)) * 100.00;
	
    }
    
    public double getReadMissRate() {
	
	return ((double) numberOfReadMisses / ((double) numberOfReadOperations)) * 100.00;
	
    }
    
    public double getReadCoherenceMissRate() {
	
	return ((double) readCoherenceMiss / ((double) numberOfReadMisses)) * 100.00;
	
    }
    
    public double getWriteCoherenceMissRate() {
	
	return ((double) writeCoherenceMiss / ((double) numberOfWriteMisses)) * 100.00;
	
    }

    /**
     * 
     * @return calculated Write Hit Rate Percentage
     */
    public double getWriteHitRate() {
	
	return ((double) numberOfWriteHits / (numberOfWriteOperations)) * 100;
    }
    
    public double getWriteMissRate() {
	
	return ((double) numberOfWriteMisses / (numberOfWriteOperations)) * 100;
    }

    /**
     * 
     * @return combined (Read and Write) hit Rate Percentage
     */
    public double getCombinedHitRate() {
	
	return (((double) numberOfWriteHits + (double) numberOfReadHits) / ((double) numberOfWriteOperations + numberOfReadOperations)) * 100;
    }
    
    public double getCombinedMissRate() {
	
	return (((double) numberOfWriteMisses + (double) numberOfReadMisses) / ((double) numberOfWriteOperations + numberOfReadOperations)) * 100;
    }
    
    public double getCombinedCoherenceMissRate() {
	
	return (((double) readCoherenceMiss + (double) writeCoherenceMiss) / ((double) numberOfWriteMisses + numberOfReadMisses)) * 100;
    }

    /*
     * 
     *This method is used to give a reference for each Process to all other Processors.  The requirement for
     * this is so that the BusWriteMiss and the BusReadMiss Methods can be called directly from the Process.
     * 
     */
    void makeAquaintance(ProcessorCache[] pC) {
	listOfProcessors = pC;
	
    }

    /**
     * 
     * @param index
     * @param type
     * @return
     */
    public int getPrivateCacheLines(int index, int type) {
	
	switch (type) {
	    case 0:
		return privateCacheLines[index];
	    case 1:
		return privateReadCacheLines[index];
	    case 2:
		return privateWriteCacheLines[index];
	    
	}
	return privateCacheLines[index];
    }
    
    private boolean foundInQueue(int index, int tag) {
	boolean result = false;
	
	if (TSO) {
	    currentIndexTag[0] = index;
	    currentIndexTag[1] = tag;
	    
	
	    
	    
	    for (int[] iter : storeBuffer) {
		if (iter[0] == currentIndexTag[0] && iter[1] == currentIndexTag[1]) {
		    result = true;
		}
	    }
	    
	    
	    
	} else {
	    
	    result = false;
	}
	
	return result;
	
    }
    
    private void checkWriteBuffer() {
	
	if (storeBuffer.size() >= retirementPolicy) {
	    
	    if (newData) {
		int[] tempIndexTag = storeBuffer.peek();
		
		
		
		
		
		expectedCompleteTime = ProcessWrite(tempIndexTag[0], tempIndexTag[1], tempIndexTag[2]) + processorLatency;
		
		newData = false;
	    } else {
		
		if (processorLatency >= (expectedCompleteTime)) {
		    storeBuffer.poll();
		    System.out.printf("WRITE HAS FINISHED, NEW WRITE CAN HAPPEN!\n\n");
		    newData = true;
		}
		
	    }
	    
	}
    }
    
    private void printQueue(){
	
	
    }

    void emptyStoreBuffer() {
	
	if(storeBuffer.size() >= retirementPolicy){
	    storeBuffer.poll();
	}
	
	System.out.println("EMPTY STORE BUFFER");
	
	for (int[] iter : storeBuffer) {
		
		processorLatency += ProcessWrite(iter[0], iter[1], iter[2]);
		System.out.printf("Current Latency Count %d (Process %d)\n\n", processorLatency, processNumber);
	    }
	
    }
}
