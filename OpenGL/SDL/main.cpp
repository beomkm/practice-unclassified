#pragma comment (lib,"SDL2")
#pragma comment (lib,"SDL2main")
#pragma comment (lib,"SDL2_image")
#pragma comment (lib,"opengl32.lib")

#include <cstdio>
#include <SDL.h>
#include <SDL_opengl.h>
#include <SDL_image.h>

using namespace std;



int main(int argc, char **argv) {

#ifdef WIN32
	printf("windows\n");
#endif
	SDL_Window *win = NULL;
	SDL_Surface *image;
	SDL_RWops *rwop;
	rwop = SDL_RWFromFile("../res/5122.png", "rb");
	image = IMG_LoadPNG_RW(rwop);
	GLubyte *map = (GLubyte*)image->pixels;


	//Initialize SDL.
	if (SDL_Init(SDL_INIT_VIDEO) < 0)
		return 1;

	//Create the window
	win = SDL_CreateWindow("OpenGL", 100, 100, 512, 512, SDL_WINDOW_OPENGL);

	SDL_GLContext context;
	context = SDL_GL_CreateContext(win);



	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	glEnable(GL_TEXTURE_2D);


	GLuint texId;
	glGenTextures(1, &texId);
	glBindTexture(GL_TEXTURE_2D, texId);
	glTexImage2D(
		GL_TEXTURE_2D, 0, GL_RGBA,
		512, 512, 0,
		GL_RGBA, GL_UNSIGNED_BYTE,
		map
	);
	// Texture filter
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

	SDL_FreeSurface(image);

	// main loop
	while (1) {

		//event handling
		SDL_Event e;
		if (SDL_PollEvent(&e)) {
			if (e.type == SDL_QUIT)
				break;
		}
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glOrtho(0, 512, 512, 0, 0, 1);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();


		glBindTexture(GL_TEXTURE_2D, texId);
		glBegin(GL_QUADS);

		glTexCoord2f(0.0, 0.0); glVertex2f(0.0, 0.0);
		glTexCoord2f(1.0, 0.0); glVertex2f(512.0, 0.0);
		glTexCoord2f(1.0, 1.0); glVertex2f(512.0, 512.0);
		glTexCoord2f(0.0, 1.0); glVertex2f(0.0, 512.0);
		glEnd();

		
		SDL_GL_SwapWindow(win);
		SDL_Delay(16);

	}
	glDeleteTextures(1, &texId);

	SDL_GL_DeleteContext(context);
	SDL_DestroyWindow(win);
	SDL_Quit();
	
	return 0;

}