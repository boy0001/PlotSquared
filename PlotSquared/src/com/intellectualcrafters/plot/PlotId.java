package com.intellectualcrafters.plot;

public class PlotId {
	/**
	 * x value
	 */
	public Integer x;
	/**
	 * y value
	 */
	public Integer y;

	/**
	 * PlotId class (PlotId x,y values do not correspond to Block locations)
	 * 
	 * @param x
	 *            The plot x coordinate
	 * @param y
	 *            The plot y coordinate
	 */
	public PlotId(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PlotId other = (PlotId) obj;
		return (((int) this.x == (int) other.x) && ((int) this.y == (int) other.y));
	}

	@Override
	public String toString() {
		return this.x + ";" + this.y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.x;
		result = (prime * result) + this.y;
		return result;
	}
}
