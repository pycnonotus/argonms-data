/*
 *  KvJ Compiler for XML WZ data files
 *  Copyright (C) 2010  GoldenKevin
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kvjcompiler.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import kvjcompiler.Converter;
import kvjcompiler.LittleEndianWriter;
import kvjcompiler.Size;
import kvjcompiler.map.structure.*;

public class MapConverter extends Converter {
	private static final byte
		TOWN = 1,
		RETURN_MAP = 2,
		FORCED_RETURN = 3,
		MOB_RATE = 4,
		DEC_HP = 5,
		TIME_LIMIT = 6,
		PROTECT_ITEM = 7,
		EVERLAST = 8,
		LIFE = 9,
		AREA = 10,
		CLOCK = 11,
		BOAT = 12,
		REACTOR = 13,
		FOOTHOLD = 14,
		PORTAL = 15
	;
	
	public WzType getWzType() {
		return WzType.MAP;
	}
	
	public byte[] handleSpecial(String name, XMLStreamReader r) throws XMLStreamException {
		if (name.equals("portal")) {
			int portalid = Integer.parseInt(r.getAttributeValue(0));
			Portal p = new Portal(portalid);
			int open = 1;
			int event;
			while (open > 0) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open++;
					p.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
				} else if (event == XMLStreamReader.END_ELEMENT) {
					open--;
				}
			}
			LittleEndianWriter lew = new LittleEndianWriter(p.size() + Size.BYTE, PORTAL);
			p.writeBytes(lew);
			return lew.toArray();
		} else if (name.equals("life")) {
			int lifeid = Integer.parseInt(r.getAttributeValue(0));
			Life l = new Life(lifeid);
			int open = 1;
			int event;
			while (open > 0) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open++;
					l.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
				} else if (event == XMLStreamReader.END_ELEMENT) {
					open--;
				}
			}
			LittleEndianWriter lew = new LittleEndianWriter(l.size() + Size.BYTE, LIFE);
			l.writeBytes(lew);
			return lew.toArray();
		} else if (name.equals("area")) {
			String areaid = r.getAttributeValue(0);
			Area a = new Area(areaid);
			int open = 1;
			int event;
			while (open > 0) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open++;
					a.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
				} else if (event == XMLStreamReader.END_ELEMENT) {
					open--;
				}
			}
			LittleEndianWriter lew = new LittleEndianWriter(a.size() + Size.BYTE, AREA);
			a.writeBytes(lew);
			return lew.toArray();
		} else if (name.equals("reactor")) {
			int reactorid = Integer.parseInt(r.getAttributeValue(0));
			Reactor rt = new Reactor(reactorid);
			int open = 1;
			int event;
			while (open > 0) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open++;
					rt.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
				} else if (event == XMLStreamReader.END_ELEMENT) {
					open--;
				}
			}
			LittleEndianWriter lew = new LittleEndianWriter(rt.size() + Size.BYTE, REACTOR);
			rt.writeBytes(lew);
			return lew.toArray();
		} else if (name.equals("foothold")) {
			Map<Integer, Foothold> footholds = new HashMap<Integer, Foothold>();
			int size = 0;
			int open = 1;
			int event;
			Foothold f;
			int footholdid;
			
			int open2 = 1, open1 = 1;
			while (open2 > 0) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open2++;
					if (r.getLocalName().equals("imgdir")) {
						open1 = 1;
						while (open1 > 0) {
							event = r.next();
							if (event == XMLStreamReader.START_ELEMENT) {
								open1++;
								if (r.getLocalName().equals("imgdir")) {
									open = 1;
									footholdid = Integer.parseInt(r.getAttributeValue(0));
									f = new Foothold(footholdid);
									while (open > 0) {
										event = r.next();
										if (event == XMLStreamReader.START_ELEMENT) {
											open++;
											f.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
										}
										if (event == XMLStreamReader.END_ELEMENT) {
											open--;
										}
									}
									size += f.size() + Size.HEADER;
									footholds.put(Integer.valueOf(footholdid), f);
								}
							}
							if (event == XMLStreamReader.END_ELEMENT) {
								open1--;
							}
						}
					}
				}
				if (event == XMLStreamReader.END_ELEMENT) {
					open2--;
				}
			}
				
			LittleEndianWriter lew = new LittleEndianWriter(size);
			for (Entry<Integer, Foothold> pair : footholds.entrySet()) {
				lew.writeByte(FOOTHOLD);
				pair.getValue().writeBytes(lew);
			}
			return lew.toArray();
		} else {
			return null;
		}
	}
	
	public byte[] getEncodedBytes(String key, String value) {
		String[] keys = key.split("/");
		if (keys[0].equals("info")) {
			if (keys[1].equals("town")) {
				if (Integer.parseInt(value) == 1)
					return new LittleEndianWriter(Size.HEADER, TOWN).toArray();
			} else if (keys[1].equals("returnMap")) {
				return new LittleEndianWriter(Size.HEADER + Size.INT, RETURN_MAP).writeInt(Integer.parseInt(value)).toArray();
			} else if (keys[1].equals("forcedReturn")) {
				return new LittleEndianWriter(Size.HEADER + Size.INT, FORCED_RETURN).writeInt(Integer.parseInt(value)).toArray();
			} else if (keys[1].equals("mobRate")) {
				return new LittleEndianWriter(Size.HEADER + Size.FLOAT, MOB_RATE).writeFloat(Float.parseFloat(value)).toArray();
			} else if (keys[1].equals("decHP")) {
				return new LittleEndianWriter(Size.HEADER + Size.INT, DEC_HP).writeInt(Integer.parseInt(value)).toArray();
			} else if (keys[1].equals("timeLimit")) {
				return new LittleEndianWriter(Size.HEADER + Size.INT, TIME_LIMIT).writeInt(Integer.parseInt(value)).toArray();
			} else if (keys[1].equals("protectItem")) {
				return new LittleEndianWriter(Size.HEADER + Size.INT, PROTECT_ITEM).writeInt(Integer.parseInt(value)).toArray();
			} else if (keys[1].equals("everlast")) {
				if (Integer.parseInt(value) == 1)
					return new LittleEndianWriter(Size.HEADER, EVERLAST).toArray();
			}
		} else if (keys[0].equals("clock")) {
			return new LittleEndianWriter(Size.HEADER, CLOCK).toArray();
		} else if (keys[0].equals("shipObj")) {
			return new LittleEndianWriter(Size.HEADER, BOAT).toArray();
		}
		return null;
	}
	
	public void finished() {
	}
}
