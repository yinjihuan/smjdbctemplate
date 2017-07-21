# smjdbctemplate 基于 spring jdbctemplate 做的升级版
大家自己下载源码编译安装到本地仓库即可使用
```
<!-- jdbc orm -->
<dependency>
  <groupId>com.cxytiandi</groupId>
  <artifactId>cxytiandi-jdbc</artifactId>
  <version>1.0.0</version>
</dependency>
```

# 比jdbctemplate有哪些优势
- 重新定义了CxytiandiJdbcTemplate类，集成自JdbcTemplate
- 没有改变原始JdbcTemplate的功能
- 增加了orm框架必备的操作对象来管理数据
- 简单的数据库操作使用CxytiandiJdbcTemplate提高效率

# 用法如下
定义数据表对应的PO类,表名和字段名以注解中的value为准
```
@TableName(value="loudong", desc="楼栋表", author="yinjihuan")
public class LouDong implements Serializable {
	
	private static final long serialVersionUID = -6690784263770712827L;

	@Field(value="id", desc="主键ID")
	private String id;
	
	@Field(value="name", desc="小区名称")
	private String name;
	
	@Field(value="city", desc="城市")
	private String city;
	
	@Field(value="region", desc="区域")
	private String region;
	
	@Field(value="ld_num", desc="楼栋号")
	private String ldNum;
	
	@Field(value="unit_num", desc="单元号")
	private String unitNum;

	public LouDong() {
		super();
	}

	//省略get,set方法
	
	public final static String[] SHOW_FIELDS = new String[]{ "city", "region", "name", "ld_num" };
	
	public final static String[] QUERRY_FIELDS = new String[]{ "city", "region", "name" };
	
	public final static Orders[] ORDER_FIELDS = new Orders[] { new Orders("id", Orders.OrderyType.ASC) };
}

```
---
```
@Service
public class LdServiceImpl extends EntityService<LouDong> implements LdService {

	public long count() {
		return super.count();
	}

	public List<LouDong> findAll() {
		return super.list(LouDong.ORDER_FIELDS);
	}

	public List<LouDong> find(String city) {
		return super.list("city", city);
	}

	public List<LouDong> find(String city, String region) {
		return super.list(new String[]{"city", "region"}, new Object[] {city, region});
	}

	public List<LouDong> find(String city, String region, String name) {
		return super.list(LouDong.SHOW_FIELDS, LouDong.QUERRY_FIELDS, new Object[] {city, region, name});
	}

	public List<LouDong> findAll(PageQueryParam page) {
		return super.listForPage(page.getStart(), page.getLimit(), LouDong.ORDER_FIELDS);
	}

	public boolean exists(String city) {
		return super.exists("city", city);
	}

	public List<LouDong> in(String[] names) {
		return super.in(new String[]{"city", "region"}, "name", names);
	}

	public List<LouDongDo> group() {
		return super.getJdbcTemplate().list(LouDongDo.class, "select city,count(id) as count from loudong GROUP BY city");
	}

	public LouDong get(String id) {
		return super.getById("id", id);
	}

	@Transactional
	public void delete(String name) {
		super.deleteById("name", name);
	}

	public void save(LouDong louDong) {
		super.save(louDong);
	}

	@Override
	public void saveList(List<LouDong> list) {
		super.batchSave(list);
	}

	@Override
	public void update(LouDong louDong) {
		super.update(louDong, "id");
	}

	@Override
	public void updateList(List<LouDong> list) {
		super.batchUpdateByContainsFields(list, "id", "city");
	}

}
```

---
测试类
```
/**
 * 楼栋业务测试类
 * @author yinjihuan
 *
 */
public class LdServiceTest extends TestBase {
	
	@Autowired
	LdService ldService;
	
	@Test
	public void testCount() {
		System.out.println(ldService.count());
	}
	
	@Test
	public void testFindAll() {
		List<LouDong> list = ldService.findAll();
		System.out.println(JsonUtils.toJson(list));
	}
	
	@Test
	public void testFind() {
		long start = System.currentTimeMillis();
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		for (int i = 0; i < 1000; i++) {
			executorService.execute(new Runnable() {
				
				public void run() {
					List<LouDong> list = ldService.find("上海", "虹口");
					System.out.println(JsonUtils.toJson(list));
				}
			});
		}
		executorService.shutdown();
		while(!executorService.isTerminated()){}
		
		long end = System.currentTimeMillis();
		System.err.println(end - start);
		
	}
	
	@Test
	public void testFindAllByPage() {
		List<LouDong> list = ldService.findAll(new PageQueryParam(1, 10));
		System.out.println(JsonUtils.toJson(list));
	}
	
	@Test
	public void testExists() {
		System.out.println(ldService.exists("上海"));
	}
	
	@Test
	public void testIn() {
		List<LouDong> list =ldService.in(new String[] {"大二小区", "大三小区"});
		System.out.println(JsonUtils.toJson(list));
	}
	
	@Test
	public void testGroup() {
		List<LouDongDo> list =ldService.group();
		System.out.println(JsonUtils.toJson(list));
	}
	
	@Test
	public void testGet() {
		LouDong loudong =ldService.get("1001");
		System.out.println(JsonUtils.toJson(loudong));
	}
	
	@Test
	public void testDelete() {
		ldService.delete("大二小区");
	}
	
	@Test
	public void testSave() {
		LouDong louDong = new LouDong();
		louDong.setId("8888");
		louDong.setCity("长沙");
		louDong.setRegion("岳麓");
		louDong.setName("达美D6区");
		louDong.setLdNum("6");
		louDong.setUnitNum("1");
		ldService.save(louDong);
	}
	
	@Test
	public void testSaveList() {
		List<LouDong> list = new ArrayList<LouDong>();
		LouDong louDong = new LouDong();
		louDong.setId("8888");
		louDong.setCity("长沙");
		louDong.setRegion("岳麓");
		louDong.setName("达美D6区");
		louDong.setLdNum("6");
		louDong.setUnitNum("1");
		list.add(louDong);
		
		louDong = new LouDong();
		louDong.setId("8888");
		louDong.setCity("长沙");
		louDong.setRegion("岳麓");
		louDong.setName("达美D6区");
		louDong.setLdNum("6");
		louDong.setUnitNum("1");
		list.add(louDong);
		
		ldService.saveList(list);
	}
	
	@Test
	public void testUpdate() {
		LouDong louDong = new LouDong();
		louDong.setId("8888");
		louDong.setCity("长沙");
		louDong.setRegion("岳麓");
		louDong.setName("达美D6区");
		louDong.setLdNum("61");
		louDong.setUnitNum("11");
		ldService.update(louDong);
	}
	
	@Test
	public void testUpdateList() {
		List<LouDong> list = new ArrayList<LouDong>();
		LouDong louDong = new LouDong();
		louDong.setId("8888");
		louDong.setCity("长沙");
		louDong.setRegion("岳麓");
		louDong.setName("达美D6区");
		louDong.setLdNum("6");
		louDong.setUnitNum("1");
		list.add(louDong);
		
		louDong = new LouDong();
		louDong.setId("8888");
		louDong.setCity("长沙");
		louDong.setRegion("岳麓");
		louDong.setName("达美D6区");
		louDong.setLdNum("6");
		louDong.setUnitNum("1");
		list.add(louDong);
		
		ldService.updateList(list);
	}
}
```
