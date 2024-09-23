import {cloneDeep, sortBy} from "lodash-es";

export interface TreeNode<T> {
    children?: TreeNode<T>[];

    [key: string]: any;
}

/**
 * 根据属性 key 查找树形数组中匹配的某个节点
 * @param trees 属性数组
 * @param targetKey 需要匹配的属性值
 * @param customKey 默认为 key，可自定义需要匹配的属性名
 * @param dataKey
 * @returns 匹配的节点/null
 */
export function findNodeByKey<T>(
    trees: TreeNode<T>[],
    targetKey: string | number,
    customKey = 'key',
    dataKey: string | undefined = undefined
): TreeNode<T> | T | null {
    for (let i = 0; i < trees.length; i++) {
        const node = trees[i];
        if (dataKey ? node[dataKey]?.[customKey] === targetKey : node[customKey] === targetKey) {
            return node; // 如果当前节点的 key 与目标 key 匹配，则返回当前节点
        }

        if (Array.isArray(node.children) && node.children.length > 0) {
            const _node = findNodeByKey(node.children, targetKey, customKey, dataKey); // 递归在子节点中查找
            if (_node) {
                return _node; // 如果在子节点中找到了匹配的节点，则返回该节点
            }
        }
    }

    return null; // 如果在整个树形数组中都没有找到匹配的节点，则返回 null
}

/**
 * 递归遍历树形数组或树，返回新的树
 * @param tree 树形数组或树
 * @param customNodeFn 自定义节点函数
 * @param customChildrenKey 自定义子节点的key
 * @param parent 父节点
 * @param parentPath 父节点路径
 * @param level 节点层级
 * @returns 遍历后的树形数组
 */
export function mapTree<T>(
    tree: TreeNode<T> | TreeNode<T>[] | T | T[],
    customNodeFn: (node: TreeNode<T>, path: string, _level: number) => TreeNode<T> | null = (node) => node,
    customChildrenKey = 'children',
    parentPath = '',
    level = 0,
    parent: TreeNode<T> | null = null
): T[] {
    let cloneTree = cloneDeep(tree);
    if (!Array.isArray(cloneTree)) {
        cloneTree = [cloneTree];
    }

    function mapFunc(
        _tree: TreeNode<T> | TreeNode<T>[] | T | T[],
        _parentPath = '',
        _level = 0,
        _parent: TreeNode<T> | null = null
    ): T[] {
        if (!Array.isArray(_tree)) {
            _tree = [_tree];
        }
        return _tree
            .map((node: TreeNode<T>, i: number) => {
                const fullPath = node.path ? `${_parentPath}/${node.path}`.replace(/\/+/g, '/') : '';
                node.sort = i + 1; // sort 从 1 开始
                node.parent = _parent || undefined; // 没有父节点说明是树的第一层
                const newNode = typeof customNodeFn === 'function' ? customNodeFn(node, fullPath, _level) : node;
                if (newNode) {
                    newNode.level = _level;
                    if (newNode[customChildrenKey] && newNode[customChildrenKey].length > 0) {
                        newNode[customChildrenKey] = mapFunc(newNode[customChildrenKey], fullPath, _level + 1, newNode);
                    }
                }
                return newNode;
            })
            .filter((node: TreeNode<T> | null) => node !== null);
    }

    return mapFunc(cloneTree, parentPath, level, parent);
}

export const characterLimit = (str?: string) => {
    if (!str) return '';
    if (str.length <= 20) {
        return str;
    }
    return `${str.slice(0, 20 - 3)}...`;
};

/**
 * 递归遍历树形数组或树
 * @param tree 树形数组或树
 * @param customNodeFn 自定义节点函数
 * @param continueCondition 自定义子节点的key
 * @param customChildrenKey 继续递归的条件，某些情况下需要无需递归某些节点的子孙节点，可传入该条件
 */
export function traverseTree<T>(
    tree: TreeNode<T> | TreeNode<T>[] | T | T[],
    customNodeFn: (node: TreeNode<T>) => void,
    continueCondition?: (node: TreeNode<T>) => boolean,
    customChildrenKey = 'children'
) {
    if (!Array.isArray(tree)) {
        tree = [tree];
    }
    for (let i = 0; i < tree.length; i++) {
        const node = (tree as TreeNode<T>[])[i];
        if (typeof customNodeFn === 'function') {
            customNodeFn(node);
        }
        if (node[customChildrenKey] && Array.isArray(node[customChildrenKey]) && node[customChildrenKey].length > 0) {
            if (typeof continueCondition === 'function' && !continueCondition(node)) {
                // 如果有继续递归的条件，则判断是否继续递归
                break;
            }
            traverseTree(node[customChildrenKey], customNodeFn, continueCondition, customChildrenKey);
        }
    }
}

/**
 * 根据 key 遍历树，并返回找到的节点路径和节点
 */
export function findNodePathByKey<T>(
    tree: TreeNode<T>[],
    targetKey: string,
    dataKey?: string,
    customKey = 'key'
): TreeNode<T> | null {
    for (let i = 0; i < tree.length; i++) {
        const node = tree[i];
        if (dataKey ? node[dataKey]?.[customKey] === targetKey : node[customKey] === targetKey) {
            return {...node, treePath: [dataKey ? node[dataKey] : node]}; // 如果当前节点的 key 与目标 key 匹配，则返回当前节点
        }

        if (Array.isArray(node.children) && node.children.length > 0) {
            const result = findNodePathByKey(node.children, targetKey, dataKey, customKey); // 递归在子节点中查找
            if (result) {
                result.treePath.unshift(dataKey ? node[dataKey] : node);
                return result; // 如果在子节点中找到了匹配的节点，则返回该节点
            }
        }
    }
    return null;
}

/**
 * 获取 URL 哈希参数
 */
export const getHashParameters = (): Record<string, string> => {
    const query = window.location.hash.split('?')[1]; // 获取 URL 哈希参数部分
    const paramsArray = query?.split('&') || []; // 将哈希参数字符串分割成数组
    const params: Record<string, string> = {};

    // 遍历数组并解析参数
    paramsArray.forEach((param) => {
        const [key, value] = param.split('=');
        if (key && value) {
            params[key] = decodeURIComponent(value); // 解码参数值
        }
    });

    return params;
};
export const getQueryVariable = (variable: string) => {
    const urlString = window.location.href;
    const queryIndex = urlString.indexOf('?');
    if (queryIndex !== -1) {
        const query = urlString.substring(queryIndex + 1);

        // 分割查询参数
        const params = query.split('&');
        // 遍历参数，找到 _token 参数的值
        let variableValue;
        params.forEach((param) => {
            const equalIndex = param.indexOf('=');
            const variableName = param.substring(0, equalIndex);
            if (variableName === variable) {
                variableValue = param.substring(equalIndex + 1);
            }
        });
        return variableValue;
    }
};
let lastTimestamp = 0;
let sequence = 0;
/**
 * 生成 id 序列号
 * @returns
 */
export const getGenerateId = () => {
    let timestamp = new Date().getTime();
    if (timestamp === lastTimestamp) {
        sequence++;
        if (sequence >= 100000) {
            // 如果超过999，则重置为0，等待下一秒
            sequence = 0;
            while (timestamp <= lastTimestamp) {
                timestamp = new Date().getTime();
            }
        }
    } else {
        sequence = 0;
    }

    lastTimestamp = timestamp;

    return timestamp.toString() + sequence.toString().padStart(5, '0');
};
export function isArraysEqualWithOrder<T>(arr1: T[], arr2: T[]): boolean {
    if (arr1.length !== arr2.length) {
        return false;
    }
    const sortArr1 = sortBy(arr1, 'dataIndex');
    const sortArr2 = sortBy(arr2, 'dataIndex');
    for (let i = 0; i < sortArr1.length; i++) {
        const obj1 = sortArr1[i];
        const obj2 = sortArr2[i];

        // 逐一比较对象
        if (JSON.stringify(obj1) !== JSON.stringify(obj2)) {
            return false;
        }
    }
    return true;
}
export function addCommasToNumber(number: number) {
    if (number === 0 || number === undefined) {
        return '0';
    }
    // 将数字转换为字符串
    const numberStr = number.toString();
    // 分割整数部分和小数部分
    const parts = numberStr.split('.');
    const integerPart = parts[0];
    const decimalPart = parts[1] || ''; // 如果没有小数部分，则设为空字符串
    // 对整数部分添加逗号分隔
    const integerWithCommas = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    // 拼接整数部分和小数部分（如果有）
    return decimalPart ? `${integerWithCommas}.${decimalPart}` : integerWithCommas;
}