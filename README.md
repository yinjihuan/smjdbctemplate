# smjdbctemplate 基于 spring jdbctemplate 做的升级版
大家自己下载源码编译安装到本地仓库即可使用,当前版本号为1.0.2
```
<!-- jdbc orm -->
<dependency>
  <groupId>com.cxytiandi</groupId>
  <artifactId>cxytiandi-jdbc</artifactId>
  <version>1.0.2</version>
</dependency>
```

# 比jdbctemplate有哪些优势
- 重新定义了CxytiandiJdbcTemplate类，集成自JdbcTemplate
- 没有改变原始JdbcTemplate的功能
- 增加了orm框架必备的操作对象来管理数据
- 简单的数据库操作使用CxytiandiJdbcTemplate提高效率
- 支持分布式主键ID的自动生成

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

# 测试类
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

# FAQ
## 项目中怎么配置使用呢？
首先你需要有jdbctemplate的包，然后再配置smjdbc的包
如果是spring boot项目可以使用bean的方式配置
```
@Configuration
public class BeanConfig {
	
	/**
	 * JDBC
	 * @return
	 */
	@Bean(autowire=Autowire.BY_NAME)
	public CxytiandiJdbcTemplate cxytiandiJdbcTemplate() {
		return new CxytiandiJdbcTemplate("com.fangjia.model.ld.po");
	}
	
}
```
上面构造方法中传的com.fangjia.model.ld.po是你数据表对应的PO实体类所在的包路径，推荐放一个包下，如果在多个包下可以配置多个包的路径
```
@Configuration
public class BeanConfig {
	
	/**
	 * JDBC
	 * @return
	 */
	@Bean(autowire=Autowire.BY_NAME)
	public CxytiandiJdbcTemplate cxytiandiJdbcTemplate() {
		return new CxytiandiJdbcTemplate("com.fangjia.model.ld.po", "com.fangjia.model.user.po");
	}
	
}
```
如果是用xml的方式，那就用&lt; bean>标签配置即可。
```
<!-- 增强版JdbcTemplate -->
<bean id="cxytiandiJdbcTemplate" class="com.cxytiandi.jdbc.CxytiandiJdbcTemplate">
   <property name="dataSource" ref="dataSource"/>
   <constructor-arg>
      <array>
         <value>com.fangjia.model.ld.po</value>
         <value>com.fangjia.model.user.po</value>
      </array>
   </constructor-arg>
</bean>
```

> 注意：在配置CxytiandiJdbcTemplate的时候也可以不用传入对应的包信息，如果没有传入包信息，smjdbc在使用的时候会根据查询传入的实体类动态获取映射信息，推荐大家配置时传入包信息。

## 除了继承EntityService还能用什么办法使用？
大家完全可以直接注入JdbcTemplate来操作数据库，我这里只是对JdbcTemplate进行了扩展

当然也可以直接注入扩展之后的CxytiandiJdbcTemplate来操作

```
@Autowired
private CxytiandiJdbcTemplate jdbcTemplate;
```
## 支持分布式主键ID的自动生成怎么使用？
只需要在对应的注解字段上加上@AutoId注解即可，注意此字段的类型必须为String或者Long, 需要关闭数据库的自增功能,ID算法用的是[ShardingJdbc](http://shardingjdbc.io/)中的ID算法，在分布式环境下并发会出现id相同的问题，需要为每个节点配置不同的wordid即可，通过-Dsharding-jdbc.default.key.generator.worker.id=wordid设置
```
@AutoId
@Field(value="id", desc="主键ID")
private String id;
```
## 不用注解做字段名映射怎么使用？
通过@Field注解方式可以允许数据库中的字段名称跟实体类的名称不一致，通过注解的方式来映射，如果你觉得太麻烦了，那么你可以按下面的方式使用：

```
CREATE TABLE `Order`(
  id bigint(64) not null,
  shopName varchar(20) not null,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

实体类定义,只需要类名跟表名一致，属性名和字段名一致：

```
public class Order {
    private Long id;
    private String shopName;
    // get set...
}
```
## 连表查询的结果如何定义对应的实体类？
sql语句：select tab1.name,tab2.shop_name from tab1,tab2

查询出的结果肯定是name,shop_name 2个字段，这种你可以直接定义一个类，然后写上这2个字段对应的属性即可，这边有下划线定义的字段，所以我们在实体类中需要用注解来映射


```
public class Order {
    private Long name;
    @Field(value="shop_name", desc="商品名称")
    private String shopName;
    // get set...
}
```
如果不想使用注解那就在sql语句中为字段添加别名：select tab1.name,tab2.shop_name as shopName from tab1,tab2

```
public class Order {
    private Long name;
    private String shopName;
    // get set...
}
```

## 为何要封装？
有很多人问为什么要封装一个，为什么不直接用jpa或者mybatis,这个问题我是这么看的，框架这东西很多，每个人可以根据自己的喜好来使用，可以用开源的，也可以自己封装，其实我这也不算重复造轮子，因为JdbcTemplate已经封装了很好用了，我只是在上面做了一些小小的扩展而已，也没有说要去跟mybatis这些框架去做比较，我个人就是喜欢直接在代码中写SQL,JdbcTemplate符合我的开发风格，就这么简单。

# 作者
- 尹吉欢 1304489315@qq.com
- 博客 http://cxytiandi.com/blogs/yinjihuan
