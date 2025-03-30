"use strict";
const common_vendor = require("../../common/vendor.js");
const api_venue = require("../../api/venue.js");
const _sfc_main = {
  __name: "venue",
  setup(__props) {
    const searchText = common_vendor.ref("");
    const loading = common_vendor.ref(false);
    const venueList = common_vendor.ref([]);
    const scrollHeight = common_vendor.ref(0);
    const tagList = common_vendor.ref([]);
    const categories = common_vendor.ref([{ id: 0, name: "全部" }]);
    const activeTagId = common_vendor.ref(0);
    const filteredVenues = common_vendor.computed(() => {
      if (activeTagId.value === 0) {
        return venueList.value;
      } else {
        return venueList.value.filter(
          (venue) => venue.tags && venue.tags.includes(activeTagId.value)
        );
      }
    });
    const fetchTags = async () => {
      try {
        const result = await api_venue.getVenueTags();
        if (result.code === 1 && result.data) {
          tagList.value = result.data;
          categories.value = [
            { id: 0, tagName: "全部" },
            ...result.data
          ];
          console.log("标签列表:", tagList.value);
        }
      } catch (error) {
        console.error("获取标签列表失败:", error);
        common_vendor.index.showToast({
          title: "获取标签列表失败",
          icon: "none"
        });
      }
    };
    const fetchVenueList = async () => {
      if (loading.value)
        return;
      loading.value = true;
      venueList.value = [];
      try {
        const result = await api_venue.getVenueList();
        console.log("API返回数据:", result);
        if (result.code === 1 && result.data && result.data.length > 0) {
          const processedVenues = result.data.map((venue) => {
            const processedVenue = { ...venue };
            if (venue.tags && Array.isArray(venue.tags)) {
              processedVenue.tagNames = venue.tags.map((tagId) => {
                const tag = tagList.value.find((t) => t.id === tagId);
                return tag ? tag.tagName : "";
              }).filter((name) => name !== "");
            } else {
              processedVenue.tagNames = [];
            }
            return processedVenue;
          });
          venueList.value = processedVenues;
          console.log("处理后的场地列表:", venueList.value);
        } else {
          venueList.value = [];
        }
      } catch (error) {
        console.error("获取场地列表失败:", error);
        common_vendor.index.showToast({
          title: "获取场地列表失败",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    function setCategory(item) {
      if (activeTagId.value === item.id)
        return;
      activeTagId.value = item.id;
    }
    function onVenueClick(item) {
      console.log("点击场地:", item);
      ({
        id: item.id,
        tagNames: item.tagNames || []
        // 确保tagNames存在
      });
      common_vendor.index.navigateTo({
        url: `/pages/venue_detail/venue_detail?id=${item.id}`
      });
    }
    function calculateScrollHeight() {
      const info = common_vendor.index.getSystemInfoSync();
      const headerHeight = 180 * info.windowWidth / 750;
      scrollHeight.value = info.windowHeight - headerHeight;
    }
    common_vendor.onMounted(() => {
      calculateScrollHeight();
      fetchTags().then(() => {
        fetchVenueList();
      });
      common_vendor.index.onWindowResize(() => {
        calculateScrollHeight();
      });
    });
    function navigateToMyBookings() {
      common_vendor.index.navigateTo({
        url: "/pages/order/order?activeTab=venue"
      });
    }
    return (_ctx, _cache) => {
      return {
        a: searchText.value,
        b: common_vendor.o(($event) => searchText.value = $event.detail.value),
        c: common_vendor.f(categories.value, (item, index, i0) => {
          return {
            a: common_vendor.t(item.tagName),
            b: index,
            c: activeTagId.value === item.id ? 1 : "",
            d: common_vendor.o(($event) => setCategory(item), index)
          };
        }),
        d: common_vendor.f(filteredVenues.value, (item, index, i0) => {
          return common_vendor.e({
            a: item.coverImage,
            b: common_vendor.t(item.name),
            c: common_vendor.t(item.location),
            d: item.tagNames && item.tagNames.length > 0
          }, item.tagNames && item.tagNames.length > 0 ? {
            e: common_vendor.f(item.tagNames, (tagName, tagIndex, i1) => {
              return {
                a: common_vendor.t(tagName),
                b: tagIndex
              };
            })
          } : {}, {
            f: common_vendor.t(item.price),
            g: index,
            h: common_vendor.o(($event) => onVenueClick(item), index)
          });
        }),
        e: scrollHeight.value + "px",
        f: common_vendor.o(navigateToMyBookings)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c1e1bb09"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/venue/venue.vue"]]);
wx.createPage(MiniProgramPage);
