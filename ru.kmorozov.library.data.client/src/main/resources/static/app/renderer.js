import React from 'react';
import classNames from 'classnames';

const renderer = (node, treeOptions) => {
    const { id, name, loadOnDemand = false, children, state, props = {}, itemType, itemSubType } = node;
    const droppable = (treeOptions.droppable) && (props.droppable);
    const { depth, open, path, total, loading = false, selected = false } = state;
    const childrenLength = Object.keys(children).length;
    const more = node.hasChildren();

    const isDir = itemType == "storage";
    const isPdf = !isDir && (itemSubType == 'pdf');

    return (
        <div
            className={classNames(
                'infinite-tree-item',
                { 'infinite-tree-selected': selected }
            )}
            data-id={id}
            data-expanded={more && open}
            data-depth={depth}
            data-path={path}
            data-selected={selected}
            data-children={childrenLength}
            data-total={total}
            droppable={droppable}
        >
            <div
                className="infinite-tree-node"
                style={{ marginLeft: depth * 18 }}
            >
                {!more && loadOnDemand &&
                    <a className={classNames(treeOptions.togglerClass, 'infinite-tree-closed')}>
                        <i className="glyphicon glyphicon-triangle-right" />
                    </a>
                }
                {more && open &&
                    <a className={classNames(treeOptions.togglerClass)}>
                        <i className="glyphicon glyphicon-triangle-bottom" />
                    </a>
                }
                {more && !open &&
                    <a className={classNames(treeOptions.togglerClass, 'infinite-tree-closed')}>
                        <i className="glyphicon glyphicon-triangle-right" />
                    </a>
                }
                <i
                    className={classNames(
                        'infinite-tree-folder-icon',
                        'glyphicon',
                        { 'glyphicon-folder-open': isDir && open },
                        { 'glyphicon-folder-close': isDir && !open },
                        { 'glyphicon-file': !isDir }
                    )}
                />
                <span className="infinite-tree-title">{name}</span>
                <i
                    style={{ marginLeft: 5 }}
                    className={classNames(
                        { 'hidden': !loading },
                        'glyphicon',
                        'glyphicon-refresh',
                        { 'rotating': loading }
                    )}
                />
                <span className="count">{childrenLength}</span>
            </div>
        </div>
    );
};

export default renderer;
