import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Clock, CreditCard, PawPrint, MapPin, User, UserCheck } from 'lucide-react';
import { orderApi } from '../api';

interface PetInfo {
  id: number;
  name: string;
  species: string;
  breed?: string;
}

interface ServiceLogInfo {
  id: number;
  action: string;
  note?: string;
  createdAt: string;
  operatorName?: string;
}

interface OrderDetailData {
  id: number;
  orderNo: string;
  status: string;
  ownerId: number;
  ownerName?: string;
  ownerPhone?: string;
  sitterId: number | null;
  sitterName?: string;
  sitterPhone?: string;
  serviceAddress: string;
  scheduledDate: string;
  scheduledTimeStart?: string;
  scheduledTimeEnd?: string;
  totalAmount: number;
  platformFee?: number;
  sitterEarnings?: number;
  paymentStatus: string;
  createdAt: string;
  updatedAt?: string;
  pets?: PetInfo[];
  serviceLogs?: ServiceLogInfo[];
}

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING_MATCH: { label: '待匹配', color: 'bg-yellow-100 text-yellow-700' },
  PENDING_ACCEPT: { label: '待接单', color: 'bg-blue-100 text-blue-700' },
  ACCEPTED: { label: '已接单', color: 'bg-indigo-100 text-indigo-700' },
  SITTER_EN_ROUTE: { label: '前往中', color: 'bg-cyan-100 text-cyan-700' },
  IN_SERVICE: { label: '服务中', color: 'bg-green-100 text-green-700' },
  SERVICE_COMPLETED: { label: '已完成', color: 'bg-emerald-100 text-emerald-700' },
  OWNER_CONFIRMED: { label: '已确认', color: 'bg-teal-100 text-teal-700' },
  CANCELLED: { label: '已取消', color: 'bg-red-100 text-red-700' },
  DISPUTED: { label: '争议中', color: 'bg-orange-100 text-orange-700' },
};

const paymentStatusMap: Record<string, { label: string; color: string }> = {
  UNPAID: { label: '未支付', color: 'text-yellow-600' },
  PAID: { label: '已支付', color: 'text-green-600' },
  REFUNDED: { label: '已退款', color: 'text-red-600' },
  PARTIAL_REFUND: { label: '部分退款', color: 'text-orange-600' },
};

const logActionMap: Record<string, string> = {
  ORDER_CREATED: '订单创建',
  SITTER_ASSIGNED: '喂养师分配',
  SITTER_ACCEPTED: '喂养师接单',
  SITTER_REJECTED: '喂养师拒绝',
  SITTER_EN_ROUTE: '喂养师出发',
  SERVICE_STARTED: '服务开始',
  SERVICE_COMPLETED: '服务完成',
  OWNER_CONFIRMED: '主人确认',
  ORDER_CANCELLED: '订单取消',
  PAYMENT_RECEIVED: '收到付款',
  REFUND_PROCESSED: '退款处理',
};

const speciesEmoji: Record<string, string> = {
  CAT: '🐱', DOG: '🐶', BIRD: '🐦', FISH: '🐟',
  REPTILE: '🦎', SMALL_ANIMAL: '🐹', OTHER: '🐾',
};

const speciesLabel: Record<string, string> = {
  CAT: '猫', DOG: '狗', BIRD: '鸟', FISH: '鱼',
  REPTILE: '爬行动物', SMALL_ANIMAL: '小动物', OTHER: '其他',
};

export default function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderDetailData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    orderApi.get(Number(id))
      .then((res) => setOrder((res as { data: OrderDetailData }).data))
      .catch((err: Error) => setError(err.message || '加载失败'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="text-gray-400">加载中...</div>;

  if (error || !order) {
    return (
      <div className="text-center py-12">
        <p className="text-red-500 mb-4">{error || '订单不存在'}</p>
        <button onClick={() => navigate('/orders')} className="text-orange-500 hover:underline">
          返回订单列表
        </button>
      </div>
    );
  }

  const st = statusMap[order.status] ?? { label: order.status, color: 'bg-gray-100 text-gray-700' };
  const ps = paymentStatusMap[order.paymentStatus] ?? { label: order.paymentStatus, color: 'text-gray-600' };

  return (
    <div>
      <button
        onClick={() => navigate('/orders')}
        className="flex items-center gap-1 text-sm text-gray-500 hover:text-orange-500 mb-6 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> 返回订单列表
      </button>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-xl font-bold text-gray-800">订单 {order.orderNo}</h2>
            <p className="text-sm text-gray-400 mt-1">创建于 {order.createdAt}</p>
          </div>
          <span className={`px-3 py-1.5 rounded-full text-sm font-medium ${st.color}`}>
            {st.label}
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
          <div className="flex items-start gap-3">
            <div className="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center shrink-0">
              <User className="w-5 h-5 text-blue-500" />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500">宠物主人</p>
              <p className="text-gray-800 font-medium">{order.ownerName ?? `ID: ${order.ownerId}`}</p>
              {order.ownerPhone && <p className="text-sm text-gray-500">{order.ownerPhone}</p>}
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="w-10 h-10 bg-green-50 rounded-lg flex items-center justify-center shrink-0">
              <UserCheck className="w-5 h-5 text-green-500" />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500">喂养师</p>
              <p className="text-gray-800 font-medium">
                {order.sitterName ?? (order.sitterId ? `ID: ${order.sitterId}` : '未分配')}
              </p>
              {order.sitterPhone && <p className="text-sm text-gray-500">{order.sitterPhone}</p>}
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="w-10 h-10 bg-purple-50 rounded-lg flex items-center justify-center shrink-0">
              <MapPin className="w-5 h-5 text-purple-500" />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500">服务地址</p>
              <p className="text-gray-800">{order.serviceAddress}</p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="w-10 h-10 bg-cyan-50 rounded-lg flex items-center justify-center shrink-0">
              <Clock className="w-5 h-5 text-cyan-500" />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500">预约时间</p>
              <p className="text-gray-800">
                {order.scheduledDate}
                {order.scheduledTimeStart && ` ${order.scheduledTimeStart}`}
                {order.scheduledTimeEnd && ` - ${order.scheduledTimeEnd}`}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2 mb-4">
            <PawPrint className="w-5 h-5 text-orange-500" /> 服务宠物
          </h3>
          {order.pets && order.pets.length > 0 ? (
            <div className="space-y-3">
              {order.pets.map((pet) => (
                <div key={pet.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <span className="text-lg">{speciesEmoji[pet.species] ?? '🐾'}</span>
                  <div>
                    <p className="font-medium text-gray-800">{pet.name}</p>
                    <p className="text-xs text-gray-500">
                      {speciesLabel[pet.species] ?? pet.species}
                      {pet.breed && ` · ${pet.breed}`}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-400 text-sm">暂无宠物信息</p>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2 mb-4">
            <CreditCard className="w-5 h-5 text-orange-500" /> 支付信息
          </h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-gray-500">支付状态</span>
              <span className={`font-medium ${ps.color}`}>{ps.label}</span>
            </div>
            <div className="border-t border-gray-100 pt-4 space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-500">订单总额</span>
                <span className="text-gray-800 font-bold text-lg">¥{order.totalAmount}</span>
              </div>
              {order.platformFee != null && (
                <div className="flex justify-between">
                  <span className="text-gray-500">平台服务费</span>
                  <span className="text-gray-600">¥{order.platformFee}</span>
                </div>
              )}
              {order.sitterEarnings != null && (
                <div className="flex justify-between">
                  <span className="text-gray-500">喂养师收入</span>
                  <span className="text-gray-600">¥{order.sitterEarnings}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2 mb-6">
          <Clock className="w-5 h-5 text-orange-500" /> 服务日志
        </h3>
        {order.serviceLogs && order.serviceLogs.length > 0 ? (
          <div className="relative">
            <div className="absolute left-3 top-2 bottom-2 w-px bg-gray-200" />
            <div className="space-y-6">
              {order.serviceLogs.map((log, idx) => (
                <div key={log.id} className="flex items-start gap-4 relative">
                  <div
                    className={`w-6 h-6 rounded-full flex items-center justify-center shrink-0 z-10 ${
                      idx === 0 ? 'bg-orange-500' : 'bg-gray-300'
                    }`}
                  >
                    <div className="w-2 h-2 bg-white rounded-full" />
                  </div>
                  <div className="flex-1 pb-2">
                    <p className="font-medium text-gray-800">
                      {logActionMap[log.action] ?? log.action}
                    </p>
                    {log.note && <p className="text-sm text-gray-500 mt-0.5">{log.note}</p>}
                    <p className="text-xs text-gray-400 mt-1">
                      {log.createdAt}
                      {log.operatorName && ` · ${log.operatorName}`}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <p className="text-gray-400 text-sm">暂无服务日志</p>
        )}
      </div>
    </div>
  );
}
