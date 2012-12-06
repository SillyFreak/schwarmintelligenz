#include <time.h>
#define PINK 0
#define ORANGE 1
#define GREEN 2
#define BLUE 4

#define TRACKSIZETHRESHOLD 10
#define MAXSPEED 200
#define TURNSPEED 3

void drive_random()
{	
	srand ( time(NULL) );
	int direction=rand()%361-180;	//-180 to +180
	if(direction<0)
	{
		set_create_total_angle(0);
		create_drive_direct(-200,200);
		while(get_create_total_angle(0.1)>direction);
	}
	else
	{
		set_create_total_angle(0);
		create_drive_direct(200,-200);
		while(get_create_total_angle(0.1)<direction);
	}

	int i=0;
	while(i<1000)
	{
		if(get_create_lbump(0.1)||get_create_rbump(0.1))	//if bumper pressed
		{
			i=2000;
		}
		else
		{
			create_drive_direct(300,300);
		}
		i++;
		msleep(1);	
	}
	
	create_stop();
}









void track(int color)
{	
	track_update();
	if(track_size(color,0) > TRACKSIZETHRESHOLD)	//if object with color is visible
	{
		create_drive_direct(MAXSPEED+(track_x(color,0)-80)*TURNSPEED,MAXSPEED-(track_x(color,0)-80)*TURNSPEED);
	}
	else					//if object with color is not visible
	{
		create_drive_direct(200,-200);
	}
}






// reads an unsigned byte 0-255
int serial_get_byte() 
{
    	char byte;
    	if (serial_read(&byte, 1))
	{
        	return (unsigned char)byte;
	}
    	else
	{
        	return -1;
	}
}
 
// writes an unsigned byte 0-255
void serial_put_byte(unsigned char ubyte) 
{
    	char byte = (char)ubyte;
    	while (!serial_write(&byte, 1)) { }
}

// gets create's charge in mAh 0-3000
uint16_t create_get_charge()
{
    	int b;
    	char a=17;
    	serial_put_byte((unsigned)142);
    	serial_put_byte((unsigned)25);
    	while (a>16)
	{
        	sleep(0.1);
        	a=serial_get_byte();
    	}
    	b=(a<<8)|(serial_get_byte());
    	return b;
}

void charge()
{
	printf("\nemty charge-function\n");
	msleep("100");
}


