<template>
  <div class="dashboard-container">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statistics" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="card-header">
            <count-to
              :start-val="0"
              :end-val="item.value"
              :duration="2000"
              class="card-value"
            />
            <div class="card-title">{{ item.title }}</div>
          </div>
          <div class="card-footer">
            <span>
              较昨日
              <i :class="item.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'" 
                 :style="{ color: item.trend >= 0 ? '#67C23A' : '#F56C6C' }">
                {{ Math.abs(item.trend) }}%
              </i>
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>访问趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">本年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <div ref="visitChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户分布</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="userChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近活动 -->
    <el-row class="activity-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
              :timestamp="activity.time"
              :type="activity.type"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { CountTo } from 'vue3-count-to'

// 统计数据
const statistics = ref([
  { title: '今日用户数', value: 2341, trend: 5.2 },
  { title: '活跃用户', value: 1280, trend: -2.3 },
  { title: '订单总数', value: 3752, trend: 8.4 },
  { title: '总收入', value: 25680, trend: 12.5 }
])

// 时间范围选择
const timeRange = ref('week')

// 最近活动
const recentActivities = ref([
  { content: '张三发布了新的场地信息', time: '2024-03-16 10:30:00', type: 'primary' },
  { content: '系统更新完成', time: '2024-03-16 09:15:00', type: 'success' },
  { content: '李四提交了新的稿件', time: '2024-03-16 08:45:00', type: 'info' },
  { content: '新用户注册通知', time: '2024-03-16 08:00:00', type: 'warning' }
])

// 图表实例
let visitChart = null
let userChart = null

// 初始化访问趋势图表
const initVisitChart = () => {
  const chartDom = document.querySelector('.chart')
  visitChart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['访问量', '用户量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        data: [120, 132, 101, 134, 90, 230, 210]
      },
      {
        name: '用户量',
        type: 'line',
        smooth: true,
        data: [220, 182, 191, 234, 290, 330, 310]
      }
    ]
  }
  visitChart.setOption(option)
}

// 初始化用户分布图表
const initUserChart = () => {
  const chartDom = document.querySelectorAll('.chart')[1]
  userChart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '用户分布',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 1048, name: '北京' },
          { value: 735, name: '上海' },
          { value: 580, name: '广州' },
          { value: 484, name: '深圳' },
          { value: 300, name: '其他' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  userChart.setOption(option)
}

onMounted(() => {
  initVisitChart()
  initUserChart()
  
  // 响应式调整
  window.addEventListener('resize', () => {
    visitChart?.resize()
    userChart?.resize()
  })
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;

  .stat-card {
    .card-header {
      text-align: center;
      
      .card-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
      
      .card-title {
        font-size: 14px;
        color: #909399;
        margin-top: 10px;
      }
    }
    
    .card-footer {
      margin-top: 20px;
      text-align: center;
      font-size: 12px;
      color: #909399;
    }
  }

  .chart-row {
    margin-top: 20px;
    
    .chart-container {
      height: 350px;
      
      .chart {
        width: 100%;
        height: 100%;
      }

    }
  }

  .activity-row {
    margin-top: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style> 