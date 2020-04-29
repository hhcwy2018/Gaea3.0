package com.wh.gaea.industry.info;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProductionCalendar {
	public enum WorkTimePrecision {
		wpSecond, wpMinute, wpHour, wpDay, wpMonth, wpYear
	}

	public enum WorkTimeType{
		wtDate, wtTime, wtDateTime
	}
	
	public enum State{
		stWork(0, "工作时间"), stRest(1, "休息时间");

		private int code;
		private String msg;

		private State(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
	}
	
	public static class WorkTime {
		WorkTimeType workTimeType = WorkTimeType.wtDate;
		WorkTimePrecision precision = WorkTimePrecision.wpDay;
		Date start;
		Date end;
		String name;
		State state = State.stWork;
		
		public WorkTimeType getWorkTimeType() {
			return workTimeType;
		}

		public WorkTimePrecision getPrecision() {
			return precision;
		}

		public Date getStart() {
			return start;
		}

		public Date getEnd() {
			return end;
		}

		public WorkTime(JSONObject data) throws ParseException {
			if (data.has("name"))
				name = data.getString("name");
			if (data.has("workTimeType"))
				workTimeType = WorkTimeType.valueOf(data.getString("workTimeType"));
			if (data.has("state"))
				state = State.valueOf(data.getString("state"));
			if (data.has("precision"))
				precision = WorkTimePrecision.valueOf(data.getString("precision"));
			if (data.has("start"))
				start = new Date(data.getLong("start"));
			if (data.has("end"))
				end = new Date(data.getLong("end"));
			
		}
		
		public WorkTime(String name, Date start, Date end) throws ParseException {
			this(name, State.stWork, start, end);
		}
		
		public WorkTime(String name, State state, Date start, Date end) throws ParseException {
			this(name, state, WorkTimeType.wtDate, WorkTimePrecision.wpDay, start, end);
		}
		
		public WorkTime(String name, State state, WorkTimeType workTimeType, WorkTimePrecision precision, Date start, Date end) throws ParseException {
			this.precision = precision;
			this.workTimeType = workTimeType;
			this.start = format(start);
			this.end = format(end);
			this.name = name;
			this.state = state;
		}

		
		public Date format(Date time) throws ParseException {
			String dateFormat = "";
			switch (workTimeType) {
			case wtDate:
				switch (precision) {
				case wpMonth:
					dateFormat = "yyyy-MM";
					break;
				case wpYear:
					dateFormat = "yyyy";
					break;
				case wpDay:
				default:
					dateFormat = "yyyy-MM-dd";
					break;
				}
				break;
			case wtDateTime:
				switch (precision) {
				case wpDay:
					dateFormat = "yyyy-MM-dd";
					break;
				case wpHour:
					dateFormat = "yyyy-MM-dd HH";
					break;
				case wpMinute:
					dateFormat = "yyyy-MM-dd HH:mm";
					break;
				case wpMonth:
					dateFormat = "yyyy-MM";
					break;
				case wpSecond:
					dateFormat = "yyyy-MM-dd HH:mm:ss";
					break;
				case wpYear:
					dateFormat = "yyyy";
					break;
				}
				break;
			case wtTime:
				switch (precision) {
				case wpHour:
					dateFormat = "HH";
					break;
				case wpMinute:
					dateFormat = "HH:mm";
					break;
				case wpSecond:
				default:
					dateFormat = "HH:mm:ss";
					break;
				}
				break;
			default:
				break;
			}
			
			SimpleDateFormat formater = new SimpleDateFormat(dateFormat);
			return formater.parse(formater.format(time));
		}
		
		public boolean in(Date time) {
			try {
				time = format(time);
				return time.after(start) && time.before(end);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		public JSONObject toJson() {
			JSONObject data = new JSONObject();
			data.put("precision", precision.name());
			data.put("state", state.name());
			data.put("workTimeType", workTimeType.name());
			data.put("name", name);
			data.put("start", start.getTime());
			data.put("end", end.getTime());
			return data;
		}

		/**
		 * 返回此日历对象表示的时间，单位秒
		 * @return
		 */
		public long getTimes(){
			return (end.getTime() - start.getTime()) / 1000;
		}
	}

	Map<Date, List<WorkTime>> worktimes = new ConcurrentHashMap<>();

	/**
	 * 一年中的休息时间列表，key为日期，精度到天，value为每天的作息时间表
	 * 获取作息时间表对象
	 * @return
	 */
	public Map<Date, List<WorkTime>> getWorkTimes(){
		return worktimes;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		for (Entry<Date, List<WorkTime>> entry : worktimes.entrySet()) {
			JSONArray dayWorkTime = new JSONArray();
			for (WorkTime workTime : entry.getValue()) {
				dayWorkTime.put(workTime.toJson());
			}
			data.put(String.valueOf(entry.getKey().getTime()), dayWorkTime);
		}
		return data;
	}
	
	public ProductionCalendar() {
	}
	
	public ProductionCalendar(JSONObject data) throws ParseException {
		for (Object obj : data.names()) {
			JSONArray dayWorkTimeJson = data.getJSONArray((String) obj);
			List<WorkTime> dayWorkTime = new ArrayList<>();
			for (Object wObj : dayWorkTimeJson) {
				dayWorkTime.add(new WorkTime((JSONObject) wObj));
			}
			worktimes.put(new Date(Long.parseLong((String)obj)), dayWorkTime);
		}
	}

	public long getWorkTime(Date date){
		long result = 0;
		List<WorkTime> workTimes = getWorkTimes().get(date);
		for (WorkTime workTime: workTimes) {
			switch (workTime.state){

				case stWork:
					long ms = workTime.getTimes();
					result += ms;
					break;
				case stRest:
					break;
			}
		}

		return result;
	}
}
