package com.colcamex.www.util;

public class FilterUtility {
	
    
    /**
     * Trim the crap
     * @param input
     * @return
     */
    public static String filter(String input) {
    	if(input == null) {
    		return null;
    	}
        StringBuffer filtered = new StringBuffer(input.length());
        char c;
            for(int i=0; i<input.length(); i++) {
              c = input.charAt(i);
              if (c == '<') {
                filtered.append("");
              } else if (c == '>') {
                filtered.append("");
              } else if (c == '"') {
                filtered.append("");
              } else if (c == '&') {
                filtered.append("&amp;");
              } else {
                filtered.append(c);
              }
            }
        return(filtered.toString().trim());
    }

}
