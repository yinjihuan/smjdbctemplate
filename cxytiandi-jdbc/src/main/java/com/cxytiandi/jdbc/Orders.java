package com.cxytiandi.jdbc;

public class Orders {
	private String name;
	private OrderyType type;
	
	public Orders() {
		super();
	}
	
	public Orders(String name, OrderyType type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OrderyType getType() {
		return type;
	}

	public void setType(OrderyType type) {
		this.type = type;
	}



	public enum OrderyType {
		ASC("asc"), DESC("desc");
		
		private String type;
		
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		private OrderyType(String type) {
			this.type = type;
		}
	}
}


