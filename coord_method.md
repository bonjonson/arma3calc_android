include <iostream>
include <cmath>
using namespace std;
int main()
{
	float 
	x, //координата Х миномета
	y, //координата Y миномета
	x1, //координата X цели
	y1, //координата Y цели
	alt, //выста над уровнем моря миномета
	alt1, //высота над уровнем моря цели
	range, //расстояние до цели
	axis_angle, // математический угол относительно ближайшей оси координат и целью; это результат вычисления
	azimute, //азимут на цель
	

float coord_range(x1,x,y1,y)	// метод определения расстояния по координатам
{
	range = sqrt(pow((x1-x),2)+pow((y1-y),2)); // вычисляем расстояние между целью и минометом
	return range; //возвращаем range в основную функцию
	}
float coord_azimute(x1,x,y1,y) //метод опредления угла на цель в угловых милах; нужен просто как информация в финальном выводе на экран
{
	angle=(asin((x1-x)/(sqrt(pow((x1-x),2)+pow((y1-y),2)))))*1018.591636; //вычисляем угол между ближайшей осью координат и целью
	if(x1>0 and y1>0){  // цель находится на СВ
			azimute=0+abs(angle);
			cout << azimute;
		}
	if else(x1>0 and y1<0){  //цель находится на ЮВ
			azimute=3200-abs(angle);
			cout << azimute;
		}
	if else(x1<0 and y1<0){  //цель находится на ЮЗ
				azimute=3200+abs(angle);
				cout << azimute;
		}
	else(x1<0 and y1>0){ //цель находится на СЗ
			azimute=6400-abs(angle);
			cout << azimute;
		}
	return azimute;
}

	