package com.aql.request;

public class RequestData {

        private String standard;
        private int batch_size;
        private String code_letter;
        private String type;
	private String procedure;
	private String aql;

	public void setStandard(String pi_standard){standard=pi_standard;}
        public String getStandard(){return standard;}

	public void setBatch_size(int pi_batch_size){batch_size=pi_batch_size;}
        public int getBatch_size(){return batch_size;}

	public void setCode_letter(String pi_code_letter){code_letter=pi_code_letter;}
        public String getCode_letter(){return code_letter;}

	public void setType(String pi_type){type=pi_type;}
        public String getType(){return type;}

	public void setProcedure(String pi_procedure){procedure=pi_procedure;}
        public String getProcedure(){return procedure;}

	public void setAql(String pi_aql){aql=pi_aql;}
        public String getAql(){return aql;}
}

