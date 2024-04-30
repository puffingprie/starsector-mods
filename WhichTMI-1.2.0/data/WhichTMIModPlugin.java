package data;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.EngineSpecAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.loading.MissileSpecAPI;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.lang.StringBuilder;

public class WhichTMIModPlugin extends BaseModPlugin
	{
	private static Logger logger = Global.getLogger(WhichTMIModPlugin.class);
	@Override
	public void onApplicationLoad() throws Exception
		{
		editDescriptions();
		}


	private void editDescriptions()
		{
		SettingsAPI settings = Global.getSettings();
		
		try
			{
			logger.log(Level.INFO,"Loading weapons for vanilla?");
			editCSVData(settings.getMergedSpreadsheetDataForMod("id", "data/weapons/weapon_data.csv", "WhichTMI"));
			}
		catch (Exception e) 
			{
			logger.log(Level.INFO,"uh oh, stinky vanilla", e);
			}
		
		List<ModSpecAPI> mods = settings.getModManager().getEnabledModsCopy();
		for (ModSpecAPI mod : mods)
			{
			JSONArray csvData;
			try
				{
				csvData = settings.loadCSV("data/weapons/weapon_data.csv", mod.getId());
				}
			catch (Exception e)
				{
				continue;
				}
			
			logger.log(Level.INFO,"Loading weapons for " + mod.getId());
			editCSVData(csvData);
			}
		}
		
	public void editCSVData(JSONArray csvData)
		{
		// try reading each row in descriptions.csv
		for (int i = 0; i < csvData.length(); i++)
			{
			try
				{
				//System.out.println(csvData.get(i));
				//logger.log(Priority.INFO, csvData.get(i));
				JSONObject row = csvData.getJSONObject(i);
				String id = row.getString("id");
				if (stringNotEmptyOrNull(id))
					{
					WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
					
					String safety = getString("tmi safety tag");
										
					StringBuilder desc = new StringBuilder();
					StringBuilder data = new StringBuilder();
					
					if (stringNotEmptyOrNull(spec.getCustomAncillary()))
						{
						if (spec.getCustomAncillary().contains(safety)) continue;
						
						desc.append(spec.getCustomAncillary());
						desc.append("\n\n");
						}
					
					if (stringNotEmptyOrNull(spec.getCustomAncillaryHL()))
						data.append(spec.getCustomAncillaryHL());
					else
						{
						String currentDesc = desc.toString();
						if (stringNotEmptyOrNull(currentDesc))
							{
							// We are nearly guaranteed to have at least one format string, and it's better to sometimes have erroring doubling up '%'s than to crash.
							currentDesc = currentDesc.replace("%", "%%");
							desc = new StringBuilder(currentDesc);
							}
						}
					
					desc.append(safety);
					desc.append("\n");
					
					// Basic hidden stats
					tryPrintStatNumber("turn rate", row, desc, data);
					
					// Beam hidden stats
					// None!
						
					if (spec.isBeam())
						{
						tryPrintStatNumber("beam speed", row, desc, data);
						}
					else
						{
						// Projectile hidden stats
							{
							tryPrintStatNumber("min spread", row, desc, data);
							tryPrintStatNumber("max spread", row, desc, data);
							tryPrintStatNumber("spread/shot", row, desc, data);
							tryPrintStatNumber("spread decay/sec", row, desc, data);
						
							tryPrintStatNumber("proj speed", row, desc, data);
							}
						// Missile hidden stats
						Object proj = spec.getProjectileSpec();
						if (proj != null && proj instanceof MissileSpecAPI)
							{
							MissileSpecAPI miss = (MissileSpecAPI) proj;
							tryPrintStatNumber("proj hitpoints", row, desc, data);
							
							tryPrintStatNumber("launch speed", row, desc, data);
							tryPrintStatNumber("flight time", row, desc, data);
							
							ShipHullSpecAPI ship = miss.getHullSpec();
							if (ship != null)
								{
								EngineSpecAPI engines = ship.getEngineSpec();
								if (engines != null)
									{
								//	tryPrintStatExact("getMaxSpeed", engines.getMaxSpeed().toString(), desc, data);
									tryPrintStatExact("getAcceleration", "" + engines.getAcceleration(), desc, data);
									tryPrintStatExact("getDeceleration", "" + engines.getDeceleration(), desc, data);
									
									tryPrintStatExact("getMaxTurnRate", "" + engines.getMaxTurnRate(), desc, data);
									tryPrintStatExact("getTurnAcceleration", "" + engines.getTurnAcceleration(), desc, data);
									}
								}
							}
						}
						
					
					
					spec.setCustomAncillary(desc.toString());
					
					if (data.length() > 3 && data.substring(0,3).equals(" | "))
						data.delete(0,3);
					spec.setCustomAncillaryHL(data.toString());
					
		//			logger.log(Level.INFO, id);
		//			logger.log(Level.INFO, desc.toString());
		//			logger.log(Level.INFO, data.toString());
					}
				else logger.log(Level.INFO, row.toString());

				}
				catch (Exception e)
				{
				logger.log(Level.INFO,"uh oh, stinky", e);
				}
			}
	
		}
		
	public String getString(String string)
		{
		return Global.getSettings().getString("WeaponStat", string);
    	}
    
    public boolean stringNotEmptyOrNull(String string)
    	{
    	return string != null && !string.isEmpty() && !string.trim().isEmpty();
    	}
    	
    	
    public void tryPrintStatNumber(String info, JSONObject row, StringBuilder desc, StringBuilder data)
    	{
    	try {
    		tryPrintStatExact(info, row.getString(info), desc, data);
			}
		catch (Exception e)
			{
			logger.log(Level.INFO, "oof", e);
			}
    	}
    	
    public void tryPrintStatExact(String info, String toPrint, StringBuilder desc, StringBuilder data)
    	{
    	try {
	    	if (stringNotEmptyOrNull(toPrint))
				{
				desc.append(getString(info));
				desc.append("\n");
				
				data.append(" | ");
				data.append(toPrint);
				}
			}
		catch (Exception e)
			{
			logger.log(Level.INFO, "oof", e);
			}
    	}
	}
