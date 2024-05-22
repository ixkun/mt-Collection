@Component
public class ServiceBuilderFactory {

    
    private Map<Integer, List<InfoBuilderService>> code2InfoBuilderServiceMap = new HashMap<>();


    public RecommendLeadsInfoBuilder getInstance(LoadRecommendLeadsInfoContext context) {
        EntranceCodeEnum entranceCodeEnum = EntranceCodeEnum.getByCode(context.getEntranceCode());
        if (entranceCodeEnum == null) {
            return null;
        }
        // 不同的入口不同的决策方式
        Map<String, String> entranceCode2DecideMethodMap = Lion.getMap(MdpContextUtils.getAppKey(), LionConstant.ENTRANCE_CODE_DECIDE_RECOMMEND_BUILDER_MAP, String.class);
        if (MapUtils.isEmpty(entranceCode2DecideMethodMap)) {
            return null;
        }
        String decideMethod = entranceCode2DecideMethodMap.get(entranceCodeEnum.getCode().toString());
        switch (decideMethod) {
            case "ByCategory":
                return getInstanceByCategory(context);
            default:return null;
        }
    }

    @Autowired
    public void init(List<InfoBuilderService> recommendLeadsInfoBuilderList) {
        if (CollectionUtils.isNotEmpty(recommendLeadsInfoBuilderList)) {
            recommendLeadsInfoBuilderList.forEach(recommendBuilder -> {
                for (Integer entranceCode : recommendBuilder.getEntranceCodeList()) {
                    if (entranceCode2RecommendBuilderMap.containsKey(entranceCode)) {
                        entranceCode2RecommendBuilderMap.get(entranceCode).add(recommendBuilder);
                    } else {
                        entranceCode2RecommendBuilderMap.put(entranceCode, Lists.newArrayList(recommendBuilder));
                    }
                }
            });
        }
    }

}
