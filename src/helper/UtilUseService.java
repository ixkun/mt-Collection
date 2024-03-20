

public class XxxUtil {

  private volatile static XxxService xxxService;

  private static void init() {
		if (xxxService == null) {
			synchronized (XxxService.class) {
				xxxService = SpringLocator.getBean("xxxService");
			}
		}
	}

  //use
  public static Map<String, String> getPhoneValue(Collection<String> values) {
		if (CollectionUtils.isEmpty(values)) {
			return Maps.newHashMap();
		}
    
		init();
    
		List<String> phones = values.stream().filter(Objects::nonNull).filter(PhoneUtil::isPhone).distinct().collect(Collectors.toList());
		
    Map<String,String> phoneMap = new HashMap<>(values.size() * 2);
    
		List<String> missCachePhonse = new ArrayList<>(values.size());
		if (CollectionUtils.isNotEmpty(missCachePhonse)) {
			List<List<String>> partitions = Lists.partition(phones, 10);
			Map<String, String> missCachePhoneMap;
			if (partitions.size() == 1) {
				missCachePhoneMap = getUnifiedPhoneKeyByOriginPhoneV2(missCachePhonse);
			} else {
				missCachePhoneMap = Flowable
						.fromIterable(partitions)
						.parallel()
						.runOn(Schedulers.from(ThreadPoolUtil.getIO()))
						.map(UnifiedPhoneUtil::getUnifiedPhoneKeyByOriginPhoneV2)
						.sequential()
						.collectInto(new HashMap<>(missCachePhonse.size()),
								(BiConsumer<HashMap<String, String>, Map<String, String>>) HashMap::putAll)
						.blockingGet();
			}
			phoneMap.putAll(missCachePhoneMap);
		}
		return phoneMap;
	}
}
