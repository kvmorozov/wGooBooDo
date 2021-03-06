import React from "react";
import {Button, Icon} from "semantic-ui-react";
import {BootstrapTable, TableHeaderColumn} from "react-bootstrap-table";
import BookPreviewPopup from "./bookPreviewPopup";

class BooksList extends React.Component {

    constructor(props) {
        super(props);
    }

    update = (books) => {
        this.setState({books: books});
    }

    handleItemClick = (row) => {
        const selectedBook = this.state.books.find(book => book.id == row.id);
        this.refs.bookPreviewPopup.open(selectedBook);
    }

    bookFormatter = (cell, row) => {
        let iconName;
        switch (cell) {
            case 'PDF':
                iconName = 'file pdf outline';
                break;
            case 'DJVU':
                iconName = 'file text';
                break;
        }

        return (
            <Icon disabled name={iconName} />
        );
    }

    loadedFormatter = (cell, row) => {
        return (
            <input type='checkbox' checked={ cell } readOnly="true"/>
        );
    }

    render() {
        if (this.state == null || this.state.books == null)
            return <div/>
        else {
            const options = {
                onRowClick: this.handleItemClick,
                onRowDoubleClick: this.handleItemClick
            };

            return (
                <div>
                    <BootstrapTable ref='table' data={ this.state.books } options={ options }
                                    pagination>
                        <TableHeaderColumn dataField='format' dataFormat={ this.bookFormatter }>Format</TableHeaderColumn>
                        <TableHeaderColumn dataField='size' width='75'>Size</TableHeaderColumn>
                        <TableHeaderColumn dataField='loaded' dataFormat={ this.loadedFormatter }>Loaded</TableHeaderColumn>
                        <TableHeaderColumn dataField='title' isKey={ true }>Title</TableHeaderColumn>
                    </BootstrapTable>
                    <BookPreviewPopup ref="bookPreviewPopup"/>
                </div>
            )
        }
    }
}

export default BooksList;