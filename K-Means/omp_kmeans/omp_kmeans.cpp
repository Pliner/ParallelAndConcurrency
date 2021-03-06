#include <cmath>
#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <vector>
#include <cstdlib>
#include <omp.h>
#include <time.h>

using namespace std;

typedef vector<double> Point;
typedef vector<Point> Points;

// Gives random number in range [0..max_value]
unsigned int UniformRandom(unsigned int max_value) {
    unsigned int rnd = ((static_cast<unsigned int>(rand()) % 32768) << 17) |
                       ((static_cast<unsigned int>(rand()) % 32768) << 2) |
                       rand() % 4;
    return ((max_value + 1 == 0) ? rnd : rnd % (max_value + 1));
}

double Distance(const Point& point1, const Point& point2) {
    double distance_sqr = 0;
    int dimensions = point1.size();
    for (int i = 0; i < dimensions; ++i) {
        distance_sqr += (point1[i] - point2[i]) * (point1[i] - point2[i]);
    }
    return sqrt(distance_sqr);
}

int FindNearestCentroid(const Points& centroids, const Point& point) {
    double min_distance = Distance(point, centroids[0]);
    int centroid_index = 0;
    for (int i = 1; i < centroids.size(); ++i) {
        double distance = Distance(point, centroids[i]);
        if (distance < min_distance) {
            min_distance = distance;
            centroid_index = i;
        }
    }
    return centroid_index;
}

// Calculates new centroid position as mean of positions of 3 random centroids
Point GetRandomPosition(const Points& centroids) {
    int K = centroids.size();
    int c1 = rand() % K;
    int c2 = rand() % K;
    int c3 = rand() % K;
    int dimensions = centroids[0].size();
    Point new_position(dimensions);
    for (int d = 0; d < dimensions; ++d) {
        new_position[d] = (centroids[c1][d] + centroids[c2][d] + centroids[c3][d]) / 3;
    }
    return new_position;
}

vector<int> KMeans(const Points& data, int K) {
    int data_size = data.size();
    int dimensions = data[0].size();
    vector<int> clusters(data_size);

    // Initialize centroids randomly at data points
    Points centroids(K);
    for (int i = 0; i < K; ++i) {
        centroids[i] = data[UniformRandom(data_size - 1)];
    }

	int n_threads = omp_get_max_threads();
	vector<Points> thread_local_centroids(n_threads);
	vector< vector<int> > thread_local_centroids_sizes(n_threads);
	
	for(int i = 0; i < n_threads; ++i) {
		thread_local_centroids[i].assign(K, Point(dimensions));
		thread_local_centroids_sizes[i].assign(K, 0);
	} 

    bool converged = false;
    while (!converged) {
        converged = true;
        
        #pragma omp parallel for
        for (int i = 0; i < data_size; ++i) {
            int nearest_cluster = FindNearestCentroid(centroids, data[i]);
            if (clusters[i] != nearest_cluster) {
                clusters[i] = nearest_cluster;
                converged = false;
            }
        }
        if (converged) {
            break;
        }

        vector<int> clusters_sizes(K);
        centroids.assign(K, Point(dimensions)); 
		
		#pragma omp parallel
		{
			int t = omp_get_thread_num();
			#pragma omp parallel for
			for (int i = 0; i < data_size; ++i) {
				for (int d = 0; d < dimensions; ++d) {
					thread_local_centroids[t][clusters[i]][d] += data[i][d];
				}		
				++thread_local_centroids_sizes[t][clusters[i]];
			}
		}

		//reduce results
		for(int t = 0; t < n_threads; ++t) {
			for(int k = 0; k < K; ++k) {	
				clusters_sizes[k] += thread_local_centroids_sizes[t][k];
				thread_local_centroids_sizes[t][k] = 0;
				for(int d = 0; d < dimensions; ++d) {
					centroids[k][d] += thread_local_centroids[t][k][d];
					thread_local_centroids[t][k][d] = 0;
				}
			}
		}

        for (int i = 0; i < K; ++i) {
            if (clusters_sizes[i] != 0) {
                for (int d = 0; d < dimensions; ++d) {
                    centroids[i][d] /= clusters_sizes[i];
                }
            } else {
				centroids[i] = GetRandomPosition(centroids);
            }
        }

    }

    return clusters;
}

void ReadPoints(Points* data, ifstream& input) {
    int data_size;
    int dimensions;
    input >> data_size >> dimensions;
    data->assign(data_size, Point(dimensions));
    for (int i = 0; i < data_size; ++i) {
        for (int d = 0; d < dimensions; ++d) {
            double coord;
            input >> coord;
            (*data)[i][d] = coord;
        }
    }
}

void WriteOutput(const vector<int>& clusters, ofstream& output) {
    for (int i = 0; i < clusters.size(); ++i) {
        output << clusters[i] << endl;
    }
}

int main(int argc , char** argv) {
    if (argc != 4) {
        std::printf("Usage: %s number_of_clusters input_file output_file\n", argv[0]);
        return 1;
    }
	time_t start,end;
	time (&start);
	int K = atoi(argv[1]);

    char* input_file = argv[2];
    ifstream input;
    input.open(input_file, ifstream::in);
    if(!input) {
        cerr << "Error: input file could not be opened" << endl;
        return 1;
    }

    Points data;
    ReadPoints(&data, input);
    input.close();

    char* output_file = argv[3];
    ofstream output;
    output.open(output_file, ifstream::out);
    if(!output) {
        cerr << "Error: output file could not be opened" << endl;
        return 1;
    }

    srand(123); // for reproducible results

    vector<int> clusters = KMeans(data, K);

    WriteOutput(clusters, output);
    output.close();

	
	time (&end);
	double dif = difftime (end,start);
	printf ("Elasped time is %.2lf seconds.\r\n", dif );
	return 0;
}