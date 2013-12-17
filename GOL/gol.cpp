#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "mpi.h"

#define ALIVE 'X'
#define DEAD '.'

int toindex(int row, int col, int rowSize) {
    if (row < 0) {
        row = row + rowSize;
    } else if (row >= rowSize) {
        row = row - rowSize;
    }
    if (col < 0) {
        col = col + rowSize;
    } else if (col >= rowSize) {
        col = col - rowSize;
    }
    return row * rowSize + col;
}

void printgrid(char* grid, char* nextPartialGrid, FILE* f, int rowSize) {
	int i = 0;
    for (; i < rowSize; ++i) {
        strncpy(nextPartialGrid, grid + i * rowSize, rowSize);
        nextPartialGrid[rowSize] = 0;
        fprintf(f, "%s", nextPartialGrid);
    }
}

int main(int argc, char* argv[]) {    
	int rowSize;
    int iterations;
	int rank;
	int size;
	MPI_Init(&argc, &argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	
	char* grid;
	double startTime;
	if (rank == 0)
	{    
		startTime = MPI_Wtime();
		if (argc != 5) {
			fprintf(stderr, "Usage: %s rowSize input_file iterations output_file\n", argv[0]);
			return 1;
		}

		rowSize = atoi(argv[1]);
		iterations = atoi(argv[3]);

		FILE* input = fopen(argv[2], "r");
		grid = (char*) malloc(rowSize * rowSize * sizeof(char));
		int i = 0;
		for (; i < rowSize; ++i) {
			fscanf(input, "%s", grid + i * rowSize * sizeof(char));
		}
		fclose(input);	
	}	
	
	MPI_Bcast(&rowSize, 1, MPI_INT, 0, MPI_COMM_WORLD);	
	MPI_Bcast(&iterations, 1, MPI_INT, 0, MPI_COMM_WORLD);	
	int partSize = rowSize / size;


    char* currentPartialGrid = (char*) malloc(rowSize * (partSize+2) * sizeof(char));

	MPI_Scatter(grid, rowSize * partSize , MPI_CHAR, currentPartialGrid + rowSize, rowSize * partSize , MPI_CHAR, 0, MPI_COMM_WORLD);

    char* nextPartialGrid = (char*) malloc(rowSize * (partSize+2) * sizeof(char));
	int iteration = 0;
	for (; iteration < iterations; ++iteration) {
		MPI_Request* reqs = (MPI_Request*) malloc(4 * sizeof(MPI_Request));		
		MPI_Status* status = (MPI_Status*) malloc(4 * sizeof(MPI_Status));

		int prev = rank - 1;
		if (prev == -1)
			prev = size - 1;

		int next  = rank + 1;
		if (next == size)
			next = 0;
		char * rowToRecvFromPrev = currentPartialGrid;
		char * rowToRecvFromNext = currentPartialGrid + partSize * rowSize + rowSize;
		char * rowToSendForNext = currentPartialGrid+partSize*rowSize;
		char * rowToSendForPrev = currentPartialGrid+rowSize;
		MPI_Irecv(rowToRecvFromPrev, rowSize, MPI_CHAR, prev, 0, MPI_COMM_WORLD, &reqs[0]);
		MPI_Irecv(rowToRecvFromNext, rowSize, MPI_CHAR, next, 0, MPI_COMM_WORLD, &reqs[1]);

		MPI_Isend(rowToSendForNext, rowSize, MPI_CHAR, next, 0, MPI_COMM_WORLD, &reqs[2]);
		MPI_Isend(rowToSendForPrev, rowSize, MPI_CHAR, prev, 0, MPI_COMM_WORLD, &reqs[3]);

		MPI_Waitall(4, reqs, status);
		int i = 1;
		for (; i <= partSize; ++i) {
			int j = 0;
			for (; j < rowSize; ++j) {
                int alive_count = 0;
				int di = -1;
		        for (; di <= 1; ++di) {
					int dj = -1;
			        for (; dj <= 1; ++dj) {
                        if ((di != 0 || dj != 0) && currentPartialGrid[toindex(i + di, j + dj, rowSize)] == ALIVE) {
                            ++alive_count;
                        }
                    }
                }
                int current = i * rowSize + j;
                if (alive_count == 3 || (alive_count == 2 && currentPartialGrid[current] == ALIVE)) {
                    nextPartialGrid[current] = ALIVE;
                } else {
                    nextPartialGrid[current] = DEAD;
                }
            }
        } 
        char* tmp = currentPartialGrid; currentPartialGrid = nextPartialGrid; nextPartialGrid = tmp;
    } 
	
	MPI_Gather(currentPartialGrid + rowSize, partSize*rowSize, MPI_CHAR, grid, partSize*rowSize, MPI_CHAR, 0, MPI_COMM_WORLD);
	
	if (rank == 0)
	{
		FILE* output = fopen(argv[4], "w");
		printgrid(grid, nextPartialGrid, output, rowSize);
		fclose(output);
		double endTime = MPI_Wtime();
		printf("Total time: %f\n", endTime - startTime);
	}
	return 0;
}