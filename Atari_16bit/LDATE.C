/****************************************************/
/*  LDATE.C  (Atari 16 bit Lattice C)               */
/*  Set ST date and time using usbModem 'getdtm'    */
/*  via usb to RS-232 adapter                       */
/*                                                  */
/****************************************************/

#include <ext.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <osbind.h>

/* Device codes to pass to Bconstat(), Bconin() and Bconout() */
#define PRT 0	/* printer */
#define AUX 1	/* RS232 */
#define CON 2	/* Console (ie screen and keyboard) */
#define MIDI 3	/* Atari MIDI port */
#define KBD 4	/* Atari Keyboard port */

const int dow[] = { 0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4 };
int       dom[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
const char * mTxt[] =
     { 
	"January", "February", "March", "April", "May", "June", 
	"July", "August", "September", "October","November", "December"
     };

struct dateinfo {
  short ti_year;
  char  ti_day;
  char  ti_month;
};

struct timeinfo {
  unsigned char   ti_min;
  unsigned char   ti_hour;
  unsigned char   ti_hnd;
  unsigned char   ti_sec;
};

/* Find our DOW  */
int dayofweek(int d, int m, int y) 
{ 
    if(y%400 == 0) dom[2] = 29;
    y -= m < 3; 
    return ( y + y/4 - y/100 + y/400 + dow[m-1] + d) % 7; 
} 


int printDt() 
{
    struct dateinfo dinf;
    struct timeinfo tinf;
    getdate(&dinf);
    gettime(&tinf);
    printf("\n\n* SysDate -> %02i/%02i/%4i %02i:%02i:%02i\n  ", 
         dinf.ti_month, dinf.ti_day, dinf.ti_year,
         tinf.ti_hour,  tinf.ti_min, tinf.ti_sec );
   return 1;
}


int printCl() 
{
  int lg, dg, dinm, d, x;  
  struct dateinfo dinf;
  struct timeinfo tinf;  

  getdate(&dinf);
  gettime(&tinf);
  dinm = dom[dinf.ti_month - 1];  
  lg = dayofweek(1, dinf.ti_month, dinf.ti_year);
  
  printf("\n %s %02i %02i    %02i:%02i:%02i  \n",
        mTxt[dinf.ti_month - 1], dinf.ti_day, dinf.ti_year, 
        tinf.ti_hour,  tinf.ti_min, tinf.ti_sec );
  printf("\n Su  Mn  Tu  We  Th  Fr  Sa \n");
  
  for (d = 1; d <= 42;)
  {
	printf("\n");
	for(x=1; x<=7; x++)
    {
     dg = d - lg;
     if(dg == dinf.ti_day)
       printf(" \033p%02d\033q ", dg)
     else if(d > lg && d <= dinm + lg) 
       printf(" %02d ", dg);
     else 
       printf("    ");

    d++;     
	}		
   }
  return 1;
}

int setDt(char *i)
{
    char yr[5], mt[3], dy[3];
    struct dateinfo dinf;

    strmid(i, &yr, 1, 4);
    strmid(i, &mt, 6, 2);
    strmid(i, &dy, 9, 2);

    dinf.ti_year  = atoi(yr);
    dinf.ti_month = atoi(mt);
    dinf.ti_day   = atoi(dy);

    if (dinf.ti_year == 0 || dinf.ti_month == 0 ||  dinf.ti_day == 0 )
      return 0;

    setdate(&dinf);
    return 1;
}


int setTm(char *i)
{
    char hr[3], mn[3];
    struct timeinfo tinf;

    strmid(i, &hr, 1, 2);
    strmid(i, &mn, 4, 2);

    tinf.ti_hour  = atoi(hr);
    tinf.ti_min   = atoi(mn);
    tinf.ti_sec   = 0;
    tinf.ti_hnd   = 0;

    if(tinf.ti_hour == 0) return 0;

    settime(&tinf);
    return 1;
}

int getRS232() {
   int gt [8] = {'g','e','t','d','t','m',0x0D,0x00};
   int getting  = 3;
 
   while (getting) {
    int i;
    char st[256], dm[22], tm[22];
    char * tx;

    for(i=0; i < 7;)
      Bconout(AUX, (char) gt[i++]);
    sleep(1);
    for(i=0; i < 256 && Bconstat(AUX) != 0;)
	   st[i++] = (char) (0xff & Bconin(AUX));
    st[i++] = 0x00;

    tx = strstr(st, "getdtm");
    if(tx && strlen(tx) > 22)
    {
      strmid(tx, &dm, 7, 10);
      strmid(tx, &tm, 18, 6);
      if(setDt(&dm) && setTm(&tm))
          return 1;
    }
    getting--;
  }
  return 0;
}


int main(void)
{
    char tm[256];
    printDt();

  if(! getRS232())
   {
       printf("\n Date(yyyy mm dd): ");
       gets(tm);
       setDt(&tm);

       printf("\n Time(hh mm): ");
       gets(tm);
       setTm(&tm);
   }

    printCl();
    sleep(2);
    return 0;
}

