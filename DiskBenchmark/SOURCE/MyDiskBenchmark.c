#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <string.h>
#include <time.h>

//initializing all methods used
int createThreads(int, char[], long, long);
void *writeFile(void *); //method to write (sequential/random) to file 
void *readFile(void *); //method to read (sequential/random) from file 

//structure to store and access the required parameters in threads to read and write
struct fileData {
	char accessPattern[4];
	char fileName[25];
	long fileSize;
	long blockSize;
};

void *readFile(void *arg)
{
 	struct fileData *argnew = (struct fileData*)arg; 
	char *str = (char *)malloc(argnew->blockSize); //allocating required buffer space

	//open the input file that has been writen.
	FILE *new_file = fopen(argnew->fileName, "rb");

	//disable the buffer so that following process are executed directly to disk
    	setbuf(new_file, NULL);

   	if(new_file == NULL){
          printf("Cannot open the input file!!");
          exit(1);
   
	}
 
    	srand(time(NULL)); 
	fflush(new_file); 
 	fsync(fileno(new_file));

	if(strcmp(argnew->accessPattern,"RR") == 0)
	{
		long NUM_LOOPS = 1000000000;
		fseek(new_file ,0L , SEEK_END);
		long size = ftell(new_file); //size of the file
		for (int i = 0; i < (size/argnew->blockSize); i++) {

      		int ran_pointer = (rand()%(size-argnew->blockSize+1)); //generating random number

        	fseek(new_file , ran_pointer, SEEK_SET); //placing pointer to random location
         	fread(str, (argnew->blockSize), 1, new_file); //reading block size of from file
		fflush(new_file); 
    		}
		
	}
	else if(strcmp(argnew->accessPattern,"RS") == 0) 
	{
		for (int i = 0; i < (argnew->fileSize/argnew->blockSize); i++) {
         	fread(str, (argnew->blockSize), 1, new_file);
	fflush(new_file); 
    		}
	} 

	fflush(new_file); 

	fsync(fileno(new_file));
    
    	fclose(new_file);
    	free(str);
    
    
}

void *writeFile(void *tmpfd)
{
	//Initializing file pointer
	FILE *fp;
	
	//Accessing structure argument parameters
	struct fileData *fldata = tmpfd;
	long block = fldata->blockSize;
	long size = fldata->fileSize;
	char *data = (char *)malloc(block);

	//Creating data of block size
	memset(data,1,sizeof(data));

	//file open
	fp = fopen(fldata->fileName,"wb");
	setbuf(fp, NULL);

	if(fp == NULL)
	{
		printf("Error opening file to write");
		exit(2);
	}

	if(strcmp(fldata->accessPattern,"WS") == 0)
	{

		for(int i = 0; i<size/block; i++)
		{
			fwrite(data,block,1,fp); //writing data to file in block sizes
		}
	}
	else if(strcmp(fldata->accessPattern,"WR") == 0)
	{
		for(int i = 0; i<size/block; i++)
		{
			int rand_num = (rand()%(size-block+1)); //Random number generator
			fseek(fp,rand_num,SEEK_SET); //placing the file pointer to the generated random number byte location
			fwrite(data,block,1,fp); //writing block of data to specified location
		}
		
	}

	fflush(fp); //flushing file stream - flushing data to kernel buffers

	fsync(fileno(fp)); //saving data to disk
	
	fclose(fp); //close file
	free(data); // freeing the allocated space to avoid memory leak

	return NULL;
}

int createThreads(int threadCount, char acp[4], long bs, long data)
{
	//Initializing structure variables, threads
	struct timespec stime, etime;
	struct fileData fd[threadCount];
	pthread_t file_thread[threadCount];
	char file[threadCount][20]; //to store file names (write/read)
	//long data = 10000000000;
	
	clock_gettime(CLOCK_REALTIME, &stime); //start time
	
	//generates the names of files to which data will be written/ or read from. 
	for(int i = 1; i<=threadCount; i++)
	{
		char filename[20];
		sprintf(filename,"D%d_Filedata%d.bin",threadCount,i);
		strcpy(file[i-1],filename);
	}
	
	//For Read operations
	if((strcmp(acp,"RS")==0) || (strcmp(acp,"RR")==0))
	{
		for(int i = 0; i<threadCount; i++)
		{	
			strcpy(fd[i].accessPattern,acp); 
			fd[i].fileSize = data/(threadCount);
			fd[i].blockSize = bs;
			strcpy(fd[i].fileName, file[i]);
			//create thread and pass thread, method and its structure argument
			pthread_create(&file_thread[i], NULL, readFile, &fd[i]); 
		}			
	}			
	else if((strcmp(acp,"WS")==0) || (strcmp(acp,"WR")==0)) //For Write operations
	{
		for(int i = 0; i<threadCount; i++)
		{	
			strcpy(fd[i].accessPattern,acp);
			fd[i].fileSize = data/(threadCount);
			fd[i].blockSize = bs;
			strcpy(fd[i].fileName, file[i]);
			pthread_create(&file_thread[i],NULL,writeFile,&fd[i]);
		}
	}	

	for(int j=0; j<threadCount; j++) //joining all threads
	{	
		pthread_join(file_thread[j], NULL);
	}

	clock_gettime(CLOCK_REALTIME, &etime); //end time
	double strtime = 1000*stime.tv_sec+stime.tv_nsec/1.0e6;	
	double endtime = 1000*etime.tv_sec+etime.tv_nsec/1.0e6;	
	
	double readtime = endtime-strtime;
	double throughput = data/1.0e6/(readtime/1000);	
	double iops=(throughput/bs)*1.0e6;
	printf("Read time : %10lf\n",readtime);
	printf("Throughput is: %10lf MBps\nIOPS is: %10lf OPS/sec\n",throughput,iops);
	
	return 0;	
}

int main(int argc, char *argv[])
{
	/*4 arguments will be passed - 
		arg1 - Access Pattern - (WS/RS/WR/RR)
		arg2 - Thread Count - (1/2/4/8/12/24/48)
		arg3 - Block Size - (4KB/64KB/1MB/16MB)
		arg4 - Dataset - (1GB/10GB)

	Req: 1GB dataset is used for 4KB block size only for WR and RR 
		10GB dataset is for 64KB/1MB/16MB block size*/

	//Initialize variables

	int threadCount = 1;
	char bsize[6];
	long bs = 1000*1000;
	long data = 10000000000;
	char acp[4];	
	
	threadCount = atoi(argv[2]);

	if(strcmp(argv[3],"64KB")==0) {bs = 64000;}
	else if(strcmp(argv[3],"1MB")==0) {bs = 1000000;}
	else if(strcmp(argv[3],"16MB")==0) {bs = 16000000;}
	else if(strcmp(argv[3],"4KB")==0) {bs = 4000;}

	if(strcmp(argv[4],"10GB")==0) {data = 10000000000;}
	else if(strcmp(argv[4],"1GB")==0) {data = 1000000000;}

	// Method to form the structure variables and create threads to call specific thread functions
	createThreads(threadCount, argv[1], bs, data);

return 0;
}


