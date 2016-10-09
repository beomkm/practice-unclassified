#include <stdio.h>

int main()
{
	int arr1[] = {10, 20, 30, 40, 50, 60, 70, 80, 90};
	int (*arr2)[3] = arr1;
	
	for(int i=0; i<3; i++) {
		for(int j=0; j<3; j++) {
			printf("%d ", arr2[i][j]);
		}
	}
	
	return 0;
}
