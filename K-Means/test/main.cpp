#include <cmath>
#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <vector>
#include <cstdlib>
#include <omp.h>
#include <ctime>

using namespace std;

int main(void) {
	cout << omp_get_max_threads();
	return 0;
}