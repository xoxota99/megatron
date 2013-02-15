package com.megatron.application.tools;

public enum CursorType {
CONE,	//A Cursor that specifies Power (Height) and Area of Effect (Radius). e.g.: Terrain Raise/Lower.
POINT,	//A Cursor that specifies X/Z coordinates
GRIDCELL,	//A Cursor that specifies one or more Grid Cells (integral X/Z coordinates).
NONE	//No Cursor.
}
