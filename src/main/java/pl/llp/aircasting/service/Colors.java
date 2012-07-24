package pl.llp.aircasting.service;

/**
 * Created by ags on 18/07/12 at 17:02
 */
enum Colors
{
  RED(1000, 0, 0),
  GREEN(0, 1000, 0),
  YELLOW(500, 1000, 0),
  ORANGE(250, 700, 0),
  BLACK(0, 0, 0);

  private int red;
  private int green;
  private int blue;

  Colors(int red, int green, int blue)
  {
    this.red = red;
    this.green = green;
    this.blue = blue;
  }

  public int getRed()
  {
    return red;
  }

  public int getGreen()
  {
    return green;
  }

  public int getBlue()
  {
    return blue;
  }
}
