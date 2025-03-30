"use strict";
const common_vendor = require("../../common/vendor.js");
const api_venue = require("../../api/venue.js");
const _sfc_main = {
  __name: "venue_detail",
  setup(__props) {
    const venueDetail = common_vendor.ref({});
    common_vendor.ref(false);
    const selectedDate = common_vendor.ref("");
    const availableDates = common_vendor.ref([]);
    const timeSlots = common_vendor.ref([]);
    const selectedTimeIndices = common_vendor.ref([]);
    const calculatedPrice = common_vendor.computed(() => {
      if (!venueDetail.value.price || selectedTimeIndices.value.length === 0) {
        return "0.00";
      }
      return (venueDetail.value.price * selectedTimeIndices.value.length * 0.5).toFixed(2);
    });
    const isSlotSelected = (index) => {
      return selectedTimeIndices.value.includes(index);
    };
    const getSelectedTimeRange = () => {
      if (selectedTimeIndices.value.length === 0 || !timeSlots.value.length) {
        return "";
      }
      const startIndex = Math.min(...selectedTimeIndices.value);
      const endIndex = Math.max(...selectedTimeIndices.value);
      return `${timeSlots.value[startIndex].startTime} - ${timeSlots.value[endIndex].endTime}`;
    };
    common_vendor.onLoad(async (options) => {
      console.log("接收到的参数:", options);
      const venueId = Number(options.id);
      if (!venueId) {
        common_vendor.index.showToast({
          title: "场地ID不存在",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.showLoading({
          title: "加载中..."
        });
        const result = await api_venue.getVenueDetail(venueId);
        console.log("场地详情:", result);
        if (result.code === 1) {
          venueDetail.value = result.data;
          generateAvailableDates();
          if (availableDates.value.length > 0) {
            await fetchTimeSlots(venueId, availableDates.value[0]);
          }
        } else {
          common_vendor.index.showToast({
            title: result.msg || "获取场地详情失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("获取场地详情失败:", error);
        common_vendor.index.showToast({
          title: "获取场地详情失败",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    });
    const generateAvailableDates = () => {
      const dates = [];
      const today = /* @__PURE__ */ new Date();
      for (let i = 0; i < 7; i++) {
        const date = /* @__PURE__ */ new Date();
        date.setDate(today.getDate() + i);
        const formattedDate = formatDate(date);
        dates.push(formattedDate);
      }
      availableDates.value = dates;
      selectedDate.value = dates[0];
    };
    const formatDate = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    };
    const fetchTimeSlots = async (venueId, date) => {
      if (!venueId || !date)
        return;
      try {
        common_vendor.index.showLoading({ title: "加载时间段..." });
        console.log("请求参数:", { venueId, date });
        const result = await api_venue.getAvailableTimeSlots({
          venueId: Number(venueId),
          // 确保 venueId 是数字类型
          date: formatDate(new Date(date))
          // 确保日期格式正确
        });
        console.log("时间段数据:", result);
        if (result.code === 1 && result.data) {
          timeSlots.value = result.data;
        } else {
          timeSlots.value = [];
          common_vendor.index.showToast({
            title: result.msg || "获取时间段失败",
            icon: "none"
          });
        }
      } catch (error) {
        console.error("获取时间段失败:", error);
        common_vendor.index.showToast({
          title: "获取时间段失败",
          icon: "none"
        });
        timeSlots.value = [];
      } finally {
        common_vendor.index.hideLoading();
      }
    };
    const onDateChange = async (date) => {
      selectedDate.value = date;
      selectedTimeIndices.value = [];
      await fetchTimeSlots(venueDetail.value.id, date);
    };
    const selectTimeSlot = (index) => {
      if (!timeSlots.value[index].available) {
        return;
      }
      if (selectedTimeIndices.value.length === 0) {
        selectedTimeIndices.value.push(index);
        return;
      }
      const minIndex = Math.min(...selectedTimeIndices.value);
      const maxIndex = Math.max(...selectedTimeIndices.value);
      if (index === minIndex - 1) {
        selectedTimeIndices.value.push(index);
      } else if (index === maxIndex + 1) {
        selectedTimeIndices.value.push(index);
      } else if (index >= minIndex && index <= maxIndex) {
        selectedTimeIndices.value = selectedTimeIndices.value.filter((i) => i !== index);
      } else {
        common_vendor.index.showToast({
          title: "请选择连续的时间段",
          icon: "none"
        });
      }
      validateSelectedSlots();
    };
    const validateSelectedSlots = () => {
      if (selectedTimeIndices.value.length === 0)
        return;
      selectedTimeIndices.value.sort((a, b) => a - b);
      let isValid = true;
      const first = selectedTimeIndices.value[0];
      for (let i = 0; i < selectedTimeIndices.value.length; i++) {
        if (selectedTimeIndices.value[i] !== first + i) {
          isValid = false;
          break;
        }
        if (!timeSlots.value[selectedTimeIndices.value[i]].available) {
          isValid = false;
          break;
        }
      }
      if (!isValid) {
        selectedTimeIndices.value = [];
        common_vendor.index.showToast({
          title: "请选择连续且可用的时间段",
          icon: "none"
        });
      }
    };
    const goToBooking = () => {
      if (selectedTimeIndices.value.length === 0) {
        common_vendor.index.showToast({
          title: "请选择时间段",
          icon: "none"
        });
        return;
      }
      const sortedIndices = [...selectedTimeIndices.value].sort((a, b) => a - b);
      const startSlot = timeSlots.value[sortedIndices[0]];
      const endSlot = timeSlots.value[sortedIndices[sortedIndices.length - 1]];
      const fullStartTime = `${selectedDate.value} ${startSlot.startTime}`;
      const fullEndTime = `${selectedDate.value} ${endSlot.endTime}`;
      common_vendor.index.navigateTo({
        url: `/pages/venue_rental/venue_rental?venueId=${venueDetail.value.id}&venueName=${encodeURIComponent(venueDetail.value.name)}&startTime=${encodeURIComponent(fullStartTime)}&endTime=${encodeURIComponent(fullEndTime)}&price=${calculatedPrice.value}`
      });
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.f(venueDetail.value.imageList || [], (image, index, i0) => {
          return {
            a: image.imageUrl,
            b: index
          };
        }),
        b: common_vendor.t(venueDetail.value.name),
        c: common_vendor.t(venueDetail.value.address),
        d: venueDetail.value.tagNames && venueDetail.value.tagNames.length > 0
      }, venueDetail.value.tagNames && venueDetail.value.tagNames.length > 0 ? {
        e: common_vendor.f(venueDetail.value.tagNames, (tag, index, i0) => {
          return {
            a: common_vendor.t(tag),
            b: index
          };
        })
      } : {}, {
        f: common_vendor.t(venueDetail.value.price || "0.00"),
        g: venueDetail.value.capacity
      }, venueDetail.value.capacity ? {
        h: common_vendor.t(venueDetail.value.capacity)
      } : {}, {
        i: common_vendor.t(venueDetail.value.content),
        j: common_vendor.f(venueDetail.value.detailImageList, (detailImg, index, i0) => {
          return {
            a: index,
            b: detailImg.imageUrl
          };
        }),
        k: common_vendor.f(availableDates.value, (date, index, i0) => {
          return {
            a: common_vendor.t(date.split("-")[2]),
            b: common_vendor.t(date.split("-")[1]),
            c: common_vendor.t(date.split("-")[0].substr(2)),
            d: index,
            e: selectedDate.value === date ? 1 : "",
            f: common_vendor.o(($event) => onDateChange(date), index)
          };
        }),
        l: common_vendor.f(timeSlots.value, (slot, index, i0) => {
          return {
            a: common_vendor.t(slot.startTime),
            b: common_vendor.t(slot.endTime),
            c: index,
            d: !slot.available ? 1 : "",
            e: isSlotSelected(index) ? 1 : "",
            f: common_vendor.o(($event) => selectTimeSlot(index), index)
          };
        }),
        m: common_vendor.t(calculatedPrice.value),
        n: selectedTimeIndices.value.length > 0
      }, selectedTimeIndices.value.length > 0 ? {
        o: common_vendor.t(getSelectedTimeRange())
      } : {}, {
        p: selectedTimeIndices.value.length === 0 ? 1 : "",
        q: common_vendor.o(goToBooking)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d02c37e6"], ["__file", "C:/Users/86182/Desktop/解压小程序/前端/JieYa/pages/venue_detail/venue_detail.vue"]]);
wx.createPage(MiniProgramPage);
